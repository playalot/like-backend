package controllers

import javax.inject.Inject
import com.likeorz.models.TimelineFeed
import com.likeorz.services.store.{ MongoDBService, RedisService }
import com.likeorz.utils.{ FutureUtils, RedisCacheClient, KeyUtils }
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import com.likeorz.services.{ TagService, UserService, PostService, MarkService }
import utils.{ HelperUtils, QiniuUtil }

import scala.concurrent.Future

class FeedController @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService,
    tagService: TagService,
    markService: MarkService,
    postService: PostService,
    redisService: RedisService,
    mongoDBService: MongoDBService) extends BaseController {

  val reasonMap = Map(TimelineFeed.TypeMyPost -> -1, TimelineFeed.TypeBasedOnTag -> 1, TimelineFeed.TypeEditorPick -> 2)

  // Get home feeds, ordered by created (backup solution when v3 not working)
  def getHomeFeedsV2(timestamp: Option[Long] = None) = UserAwareAction.async { implicit request =>
    // Use phone screen width for output photo size
    val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
    val pageSize = 10
    val followPageSize = 10
    val taggedPageSize = 10

    // Update user last seen
    if (request.userId.isDefined) {
      redisService.updateActiveUser(request.userId.get)
    }

    // Get post ids from different data source
    val futureIds = if (request.userId.isDefined) {
      val recommendIds = postService.getEditorPickPostIds(pageSize, timestamp)
      val followIds = postService.getPostIdsForUser(request.userId.get, followPageSize, timestamp)
      val taggedIds = postService.getTaggedPosts(request.userId.get, taggedPageSize, timestamp)
      //      val categoryIds = Future.successful(postService.getPersonalizedPostsForUser(request.userId.get, 0.4, pageSize, timestamp))
      Future.sequence(Seq(recommendIds, followIds, taggedIds))
    } else {
      val recommendIds = postService.getEditorPickPostIds(pageSize, timestamp)
      Future.sequence(Seq(recommendIds))
    }

    // Get promoted posts(Ads)
    val ads = if (request.userId.isDefined && timestamp.isEmpty && HelperUtils.random(100) > 50)
      RedisCacheClient.srandmember(KeyUtils.postPromote).map(_.toLong)
    else List[Long]()

    postService.getUserTags(request.userId.getOrElse(-1L), taggedPageSize, timestamp).flatMap { reasonTags =>
      futureIds.flatMap { results =>

        val showIds = results.flatten.distinct.sortWith(_ > _).take(pageSize)
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
  @deprecated("Old home feeds api", "v1.1.1")
  def getHomeFeedsV1(timestamp: Option[Long] = None) = UserAwareAction.async { implicit request =>
    // Use phone screen width for output photo size
    val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
    val pageSize = 10
    val followPageSize = 10
    val taggedPageSize = 10

    if (request.userId.isDefined) {
      redisService.updateActiveUser(request.userId.get)
    }

    // Get post ids from different data source
    val futureIds = if (request.userId.isDefined) {
      val recommendIds = postService.getEditorPickPostIds(pageSize, timestamp)
      val followIds = postService.getPostIdsForUser(request.userId.get, followPageSize, timestamp)
      val taggedIds = postService.getTaggedPosts(request.userId.get, taggedPageSize, timestamp)
      //      val categoryIds = Future.successful(postService.getPersonalizedPostsForUser(request.userId.get, 0.4, pageSize, timestamp))
      Future.sequence(Seq(recommendIds, followIds, taggedIds))
    } else {
      val recommendIds = postService.getEditorPickPostIds(pageSize, timestamp)
      Future.sequence(Seq(recommendIds))
    }

    // Get promoted posts(Ads)
    val ads = if (request.userId.isDefined && timestamp.isEmpty && HelperUtils.random(100) > 50)
      RedisCacheClient.srandmember(KeyUtils.postPromote).map(_.toLong)
    else List[Long]()

    postService.getUserTags(request.userId.getOrElse(-1L), taggedPageSize, timestamp).flatMap { reasonTags =>
      futureIds.flatMap { results =>
        val showIds = results.flatten.distinct.sortWith(_ > _).take(pageSize)
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

  // Get following feeds
  def getFollowingFeeds(timestamp: Option[Long] = None) = UserAwareAction.async { implicit request =>
    if (request.userId.isDefined) {
      // Use phone screen width for output photo size
      val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)

      postService.getFollowingPosts(request.userId.get, 10, timestamp).flatMap { ids =>
        if (ids.isEmpty) {
          Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
        } else {
          // Donnot show banned user posts
          val bannedUsers = RedisCacheClient.smembers(KeyUtils.bannedUsers).map(_.toLong)
          postService.getPostsByIdsSimple(ids).map(_.filterNot(p => bannedUsers.contains(p.userId)).sortBy(-_.created)).map { posts =>
            if (posts.isEmpty) {
              success(Messages("success.found"), Json.obj("posts" -> Json.arr()))
            } else {
              // Get marks for posts from mongodb
              val marksMap = mongoDBService.getPostMarksByIds(posts.map(_.id.get))

              val postsJson = posts.map { post =>
                val marks = marksMap.getOrElse(post.id.get, Seq())
                val userInfo = userService.getUserInfoFromCache(post.userId)
                val marksJson = marks.map { mark =>
                  Json.obj(
                    "mark_id" -> mark.markId,
                    "tag" -> mark.tag,
                    "likes" -> mark.likes.size,
                    "is_liked" -> {
                      if (request.userId.isDefined) mark.likes.contains(request.userId.get) else false
                    }
                  )
                }
                Json.obj(
                  "post_id" -> post.id.get,
                  "type" -> post.`type`,
                  "content" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                  "preview" -> QiniuUtil.getSizedImage(post.content, screenWidth),
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
              success(Messages("success.found"), Json.obj("posts" -> Json.toJson(postsJson), "next" -> posts.last.created))
            }
          }
        }
      }
    } else {
      Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
    }
  }

  // Get home feeds from timeline first and then from db
  def getHomeFeedsV3(timestamp: Option[Long] = None) = UserAwareAction.async { implicit request =>
    FutureUtils.timedFuture("getHomeFeedsV3") {
      // Use phone screen width for output photo size
      val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
      val pageSize = 10

      val timelineFeeds = if (request.userId.isDefined) {
        // Update user last seen
        redisService.updateActiveUser(request.userId.get)
        // Get feeds ids from mongodb timeline
        mongoDBService.getFeedsFromTimelineForUser(request.userId.get, pageSize, timestamp).map(feed => feed.postId -> feed)
      } else Seq()

      val futureFeeds = if (timelineFeeds.size < 5) {
        FutureUtils.timedFuture("getFeedsFromDB") {
          getFeedsFromDB(request.userId, pageSize, timestamp)
        }
      } else {
        Future.successful((timelineFeeds.toMap, timelineFeeds.map(_._2.ts).min))
      }

      futureFeeds.flatMap { results =>
        val (feedsMap, nextTimestamp) = results

        for {
          blockedUsers <- if (request.userId.isDefined) userService.getBlockedUserIdsForUser(request.userId.get) else Future.successful(Seq())
          posts <- postService.getPostsByIdsSimple(feedsMap.keySet.toSeq)
        } yield {
          if (posts.isEmpty) {
            success(Messages("success.found"), Json.obj("posts" -> Json.arr()))
          } else {

            val bannedUsers = RedisCacheClient.smembers(KeyUtils.bannedUsers).map(_.toLong) ++ blockedUsers

            println(bannedUsers)

            // Filter bad posts and author who is in blacklist
            val filteredPosts = posts.filter(p => p.score.getOrElse(0) >= 0 && !bannedUsers.contains(p.userId))

            // Get marks for posts from mongodb
            val marksMap = mongoDBService.getPostMarksByIds(filteredPosts.map(_.id.get))

            val postsJson = filteredPosts.map { p =>
              (p, feedsMap(p.id.get), marksMap.getOrElse(p.id.get, Seq()))
            }.sortBy(_._2.ts).reverse.map { row =>
              val post = row._1
              val feed = row._2
              val marks = row._3
              val userInfo = userService.getUserInfoFromCache(post.userId)

              val marksJson = marks.map { mark =>
                Json.obj(
                  "mark_id" -> mark.markId,
                  "tag" -> mark.tag,
                  "likes" -> mark.likes.size,
                  "is_liked" -> {
                    if (request.userId.isDefined) mark.likes.contains(request.userId.get) else false
                  }
                )
              }
              Json.obj(
                "post_id" -> post.id.get,
                "type" -> post.`type`,
                "content" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                "preview" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                "created" -> post.created,
                "reason" -> reasonMap(feed.reason),
                "reason_tag" -> feed.tag,
                "user" -> Json.obj(
                  "user_id" -> post.userId,
                  "nickname" -> userInfo.nickname,
                  "avatar" -> QiniuUtil.getAvatar(userInfo.avatar, "small"),
                  "likes" -> userInfo.likes
                ),
                "marks" -> Json.toJson(marksJson)
              )
            }
            success(Messages("success.found"), Json.obj("posts" -> Json.toJson(postsJson), "next" -> nextTimestamp))
          }
        }
      }
    }
  }

  private def getFeedsFromDB(userId: Option[Long], pageSize: Int, timestamp: Option[Long]): Future[(Map[Long, TimelineFeed], Long)] = {
    for {
      editorFeeds <- postService.getEditorPickTimelineFeeds(pageSize, timestamp)
      myPostFeeds <- if (userId.isDefined) postService.getMyPostTimelineFeeds(userId.get, pageSize, timestamp) else Future.successful(Seq())
      taggedFeeds <- if (userId.isDefined) postService.getBasedOnTagTimelineFeeds(userId.get, pageSize, timestamp) else Future.successful(Seq())
      //      tagIds <- if (userId.isDefined) tagService.getUserSubscribeTagIds(userId.get) else Future.successful(Seq())
    } yield {
      //      val t0 = System.currentTimeMillis()
      //      val taggedFeeds = mongoDBService.findPostsByTagIds(scala.util.Random.shuffle(tagIds).take(100), pageSize, timestamp)
      //      println("ts: " + (System.currentTimeMillis() - t0))
      val orderedFeeds = if (userId.isDefined) {
        if (taggedFeeds.nonEmpty && taggedFeeds.last.ts > editorFeeds.head.ts && taggedFeeds.last.ts > myPostFeeds.headOption.map(_.ts).getOrElse(-1L)) {
          taggedFeeds
        } else {
          (editorFeeds ++ myPostFeeds ++ taggedFeeds)
            .groupBy(_.postId) // deduplicate by post id
            .map(_._2.sortBy(_.ts).reverse.head).toSeq
            .sortBy(_.ts).reverse
            .take(pageSize)
        }
      } else {
        editorFeeds
      }

      val nextTimestamp = orderedFeeds.last.ts
      val feedsMap = orderedFeeds.map(feed => feed.postId -> feed).toMap
      (feedsMap, nextTimestamp)
    }
  }

}
