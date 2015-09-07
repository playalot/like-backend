package controllers

import javax.inject.Inject

import com.likeorz.utils.{ RedisCacheClient, KeyUtils }
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import com.likeorz.services.{ UserService, PostService, MarkService }
import utils.{ HelperUtils, QiniuUtil }

import scala.concurrent.Future

class FeedController @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService,
    markService: MarkService,
    postService: PostService) extends BaseController {

  // Get home feeds, ordered by created
  def getHomeFeedsV2(timestamp: Option[Long] = None) = UserAwareAction.async { implicit request =>
    // Use phone screen width for output photo size
    val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
    val pageSize = 10
    val followPageSize = 10
    val taggedPageSize = 10

    // Get post ids from different data source
    val futureIds = if (request.userId.isDefined) {
      val recommendIds = postService.getRecommendedPosts(pageSize, timestamp)
      val followIds = postService.getMyPosts(request.userId.get, followPageSize, timestamp)
      val taggedIds = postService.getTaggedPosts(request.userId.get, taggedPageSize, timestamp)
      //      val categoryIds = Future.successful(postService.getPersonalizedPostsForUser(request.userId.get, 0.4, pageSize, timestamp))
      Future.sequence(Seq(recommendIds, followIds, taggedIds))
    } else {
      val recommendIds = postService.getRecommendedPosts(pageSize, timestamp)
      Future.sequence(Seq(recommendIds))
    }

    // Get promoted posts(Ads)
    val ads = if (request.userId.isDefined && timestamp.isEmpty && HelperUtils.random(100) > 50)
      RedisCacheClient.srandmember(KeyUtils.postPromote).map(_.toLong)
    else List[Long]()

    //    var start = System.currentTimeMillis()
    postService.getTaggedPostsTags(request.userId.getOrElse(-1L), taggedPageSize, timestamp).flatMap { reasonTags =>
      //      var elapsed = (System.currentTimeMillis() - start)
      //      println(s"Preprocessing postsTags time: $elapsed millsec")
      futureIds.flatMap { results =>
        //        elapsed = (System.currentTimeMillis() - start)
        //        println(s"Preprocessing futureIds time: $elapsed millsec")
        //        results.foreach(println)

        val showIds = results.flatten.distinct.sortWith(_ > _).take(pageSize)
        //        println(showIds)
        val pointers = results.map(_.sortWith(_ > _).headOption.getOrElse(-1L)).toArray
        if (showIds.isEmpty) {
          Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
        } else {
          results.zipWithIndex.foreach { x =>
            if (x._1.nonEmpty)
              if (x._1.head < showIds.last) {
                // All items in this list less than picked items min timestamp
                pointers(x._2) = 0L
              } else {
                x._1.foreach { id =>
                  if (id >= showIds.last) pointers(x._2) = id
                }
              }
            else
              // no items in the next page, set timestamp to -1
              pointers(x._2) = -1L
          }

          // start = System.currentTimeMillis()

          // Get posts from given ids
          postService.getPostsByIds(showIds ++ ads).flatMap { list =>
            // elapsed = (System.currentTimeMillis() - start)
            // println(s"Preprocessing time: $elapsed millsec")
            // Handle empty results
            if (list.isEmpty) {
              Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
            } else {
              val idTsMap = list.map(x => (x._1.id.get, x._1.created)).toMap
              val nextTs = idTsMap.getOrElse(showIds.last, -1L)

              val promotePosts = list.filter(p => ads.contains(p._1.id.get))
              val postList = list.filter(p => showIds.contains(p._1.id.get) && !ads.contains(p._1.id.get)).sortBy(-_._1.created).toList

              val posts = if (promotePosts.nonEmpty) HelperUtils.insertAt(promotePosts.head, HelperUtils.random(3, 3), postList) else postList

              // Filter user who is in blacklist
              val userBlacklist = RedisCacheClient.smembers(KeyUtils.bannedUsers).map(_.toLong)

              val futures = posts.filter(_._1.score.getOrElse(0) >= 0).filterNot(p => userBlacklist.contains(p._1.userId)).map { row =>
                val post = row._1
                // Find post shown reason
                var reason: Int = -1
                var reasonTag: String = ""
                if (post.userId == request.userId.getOrElse(-1L)) {
                  // User own post
                  reason = -1
                } else if (results.length >= 3 && results(2).contains(post.id.get)) {
                  // Based on tags
                  reason = 1
                  reasonTag = row._2.map(_._2).find(t => reasonTags.contains(t)).getOrElse("")
                } else if (ads.contains(post.id.get) || results.head.contains(post.id.get)) {
                  // Editor pick (Ads belong to editor pick)
                  reason = 2
                }
                //                else if (results.length >= 4 && results(3).contains(post.id.get)) {
                //                  // You maybe like
                //                  reason = 3
                //                }
                //                else if (results.length >= 2 && results(1).contains(post.id.get)) {
                //                  // Following
                //                  reason = 4
                //                }
                // println(s"post[${post.id.get}][${post.content}}] $reason $reasonTag")
                val markIds = row._2.map(_._1)
                for {
                  userInfo <- userService.getUserInfo(post.userId)
                  likedMarks <- markService.checkLikes(request.userId.getOrElse(-1L), markIds)
                } yield {
                  val marksJson = row._2.sortBy(-_._3).map { fields =>
                    Json.obj(
                      "mark_id" -> fields._1,
                      "tag" -> fields._2,
                      "likes" -> fields._3,
                      "is_liked" -> likedMarks.contains(fields._1)
                    )
                  }
                  Json.obj(
                    "post_id" -> post.id.get,
                    "type" -> post.`type`,
                    "content" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                    "created" -> post.created,
                    "reason" -> reason,
                    "reason_tag" -> reasonTag,
                    "user" -> Json.obj(
                      "user_id" -> post.userId,
                      "nickname" -> userInfo.nickname,
                      "avatar" -> QiniuUtil.getAvatar(userInfo.avatar, "small"),
                      "likes" -> userInfo.likes
                    ),
                    "marks" -> Json.toJson(marksJson)
                  )
                }
              }
              Future.sequence(futures).map { posts =>
                success(Messages("success.found"), Json.obj("posts" -> Json.toJson(posts), "next" -> nextTs))
              }
            }
          }

        }

      }
    }
  }

  // Get home feeds, ordered by created
  def getHomeFeedsV1(timestamp: Option[Long] = None) = UserAwareAction.async { implicit request =>
    // Use phone screen width for output photo size
    val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
    val pageSize = 10
    val followPageSize = 10
    val taggedPageSize = 10

    // Get post ids from different data source
    val futureIds = if (request.userId.isDefined) {
      val recommendIds = postService.getRecommendedPosts(pageSize, timestamp)
      val followIds = postService.getMyPosts(request.userId.get, followPageSize, timestamp)
      val taggedIds = postService.getTaggedPosts(request.userId.get, taggedPageSize, timestamp)
      //      val categoryIds = Future.successful(postService.getPersonalizedPostsForUser(request.userId.get, 0.4, pageSize, timestamp))
      Future.sequence(Seq(recommendIds, followIds, taggedIds))
    } else {
      val recommendIds = postService.getRecommendedPosts(pageSize, timestamp)
      Future.sequence(Seq(recommendIds))
    }

    // Get promoted posts(Ads)
    val ads = if (request.userId.isDefined && timestamp.isEmpty && HelperUtils.random(100) > 50)
      RedisCacheClient.srandmember(KeyUtils.postPromote).map(_.toLong)
    else List[Long]()

    //    var start = System.currentTimeMillis()
    postService.getTaggedPostsTags(request.userId.getOrElse(-1L), taggedPageSize, timestamp).flatMap { reasonTags =>
      //      var elapsed = (System.currentTimeMillis() - start)
      //      println(s"Preprocessing postsTags time: $elapsed millsec")
      futureIds.flatMap { results =>
        //        elapsed = (System.currentTimeMillis() - start)
        //        println(s"Preprocessing futureIds time: $elapsed millsec")
        //        results.foreach(println)
        val showIds = results.flatten.distinct.sortWith(_ > _).take(pageSize)
        //        println(showIds)
        val pointers = results.map(_.sortWith(_ > _).headOption.getOrElse(-1L)).toArray
        if (showIds.isEmpty) {
          Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
        } else {
          results.zipWithIndex.foreach { x =>
            if (x._1.nonEmpty)
              if (x._1.head < showIds.last) {
                // All items in this list less than picked items min timestamp
                pointers(x._2) = 0L
              } else {
                x._1.foreach { id =>
                  if (id >= showIds.last) pointers(x._2) = id
                }
              }
            else
              // no items in the next page, set timestamp to -1
              pointers(x._2) = -1L
          }

          // Get posts from given ids
          postService.getPostsByIds(showIds ++ ads).flatMap { list =>
            // elapsed = (System.currentTimeMillis() - start)
            // println(s"Preprocessing time: $elapsed millsec")
            // Handle empty results
            if (list.isEmpty) {
              Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
            } else {
              val idTsMap = list.map(x => (x._1.id.get, x._1.created)).toMap
              val nextTs = idTsMap.getOrElse(showIds.last, -1L)

              val promotePosts = list.filter(p => ads.contains(p._1.id.get))
              val postList = list.filter(p => showIds.contains(p._1.id.get) && !ads.contains(p._1.id.get)).sortBy(-_._1.created).toList

              val posts = if (promotePosts.nonEmpty) HelperUtils.insertAt(promotePosts.head, HelperUtils.random(3, 3), postList) else postList

              // Filter user who is in blacklist
              val userBlacklist = RedisCacheClient.smembers(KeyUtils.bannedUsers).map(_.toLong)

              val futures = posts.filter(_._1.score.getOrElse(0) >= 0).filterNot(p => userBlacklist.contains(p._1.userId)).map { row =>
                val post = row._1
                // Find post shown reason
                var reason: Int = -1
                var reasonTag: String = ""
                if (post.userId == request.userId.getOrElse(-1L)) {
                  // User own post
                  reason = -1
                } else if (results.length >= 3 && results(2).contains(post.id.get)) {
                  // Based on tags
                  reason = 1
                  reasonTag = row._2.map(_._2).find(t => reasonTags.contains(t)).getOrElse("")
                } else if (ads.contains(post.id.get) || results.head.contains(post.id.get)) {
                  // Editor pick (Ads belong to editor pick)
                  reason = 2
                }
                //                else if (results.length >= 4 && results(3).contains(post.id.get)) {
                //                  // You maybe like
                //                  reason = 3
                //                }
                //                else if (results.length >= 2 && results(1).contains(post.id.get)) {
                //                  // Following
                //                  reason = 4
                //                }
                // println(s"post[${post.id.get}][${post.content}}] $reason $reasonTag")
                val markIds = row._2.map(_._1)
                for {
                  userInfo <- userService.getUserInfo(post.userId)
                  likedMarks <- markService.checkLikes(request.userId.getOrElse(-1L), markIds)
                } yield {
                  val marksJson = row._2.sortBy(-_._3).map { fields =>
                    Json.obj(
                      "mark_id" -> fields._1,
                      "tag" -> fields._2,
                      "likes" -> fields._3,
                      "is_liked" -> likedMarks.contains(fields._1)
                    )
                  }
                  Json.obj(
                    "post_id" -> post.id.get,
                    "type" -> post.`type`,
                    "content" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                    "created" -> post.created,
                    "reason" -> reason,
                    "reason_tag" -> reasonTag,
                    "user" -> Json.obj(
                      "user_id" -> post.userId,
                      "nickname" -> userInfo.nickname,
                      "avatar" -> QiniuUtil.getAvatar(userInfo.avatar, "small"),
                      "likes" -> userInfo.likes
                    ),
                    "marks" -> Json.toJson(marksJson)
                  )
                }
              }
              Future.sequence(futures).map { posts =>
                success(Messages("success.found"), Json.obj("posts" -> Json.toJson(posts), "next" -> nextTs.toString))
              }
            }
          }

        }

      }
    }
  }

  def getFriendsFeeds(timestamp: Option[Long] = None) = UserAwareAction.async { implicit request =>
    if (request.userId.isDefined) {
      // Use phone screen width for output photo size
      val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)

      postService.getFollowingPosts(request.userId.get, 10, timestamp).flatMap { ids =>
        if (ids.isEmpty) {
          Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
        } else {
          postService.getPostsByIds(ids).flatMap { list =>
            if (list.isEmpty) {
              Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
            } else {
              val sortedList = list.sortBy(-_._1.created)
              val futures = sortedList.map { row =>
                val post = row._1
                val markIds = row._2.map(_._1)

                for {
                  userInfo <- userService.getUserInfo(post.userId)
                  likedMarks <- markService.checkLikes(request.userId.getOrElse(-1L), markIds)
                } yield {
                  val marksJson = row._2.sortBy(-_._3).map { fields =>
                    Json.obj(
                      "mark_id" -> fields._1,
                      "tag" -> fields._2,
                      "likes" -> fields._3,
                      "is_liked" -> likedMarks.contains(fields._1)
                    )
                  }
                  Json.obj(
                    "post_id" -> post.id.get,
                    "type" -> post.`type`,
                    "content" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                    "created" -> post.created,
                    "user" -> Json.obj(
                      "user_id" -> post.userId,
                      "nickname" -> userInfo.nickname,
                      "avatar" -> QiniuUtil.getAvatar(userInfo.avatar, "small"),
                      "likes" -> userInfo.likes
                    ),
                    "marks" -> Json.toJson(marksJson)
                  )
                }
              }
              Future.sequence(futures).map { posts =>
                success(Messages("success.found"), Json.obj("posts" -> Json.toJson(posts), "next" -> sortedList.last._1.created))
              }
            }
          }
        }

      }
    } else {
      Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
    }
  }
}
