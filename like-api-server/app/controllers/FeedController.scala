package controllers

import javax.inject.Inject

import com.likeorz.utils.KeyUtils
import org.joda.time.DateTime
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import services.{ PostService, MarkService }
import utils.{ HelperUtils, RedisCacheClient, QiniuUtil }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/19/15
 */
class FeedController @Inject() (
    val messagesApi: MessagesApi,
    markService: MarkService,
    postService: PostService) extends BaseController {

  def getHomeFeeds(timestamp: Option[String] = None) = UserAwareAction.async { implicit request =>
    // Use phone screen width for output photo size
    val screenWidth = request.headers.get("LIKE-SCREEN-WIDTH").getOrElse("1280").toInt
    // Parse timestamp list used for each data source
    val ts = HelperUtils.parseTimestamp(timestamp)
    // Get post ids from different data source
    val futureIds = if (request.userId.isDefined) {
      val recommendIds = postService.getRecommendedPosts(6, ts.head)
      val followIds = postService.getFollowingPosts(request.userId.get, 3, ts(1))
      val taggedIds = postService.getTaggedPosts(request.userId.get, 3, ts(2))
      val categoryIds = Future.successful(postService.getPersonalizedPostsForUser(request.userId.get, 0.5))
      Future.sequence(Seq(recommendIds, followIds, taggedIds, categoryIds))
    } else {
      val recommendIds = postService.getRecommendedPosts(20, ts.head)
      Future.sequence(Seq(recommendIds))
    }

    // Get promoted posts(Ads)

    val ads = if (request.userId.isDefined && timestamp.isEmpty && HelperUtils.random(100) > 50)
      RedisCacheClient.srandmember(KeyUtils.postPromote).map(_.toLong) else List[Long]()

    futureIds.flatMap { results =>
      val resultIds = results.flatten.toSet ++ ads
      // Unique post ids
      val uniqueIds = if (timestamp.isDefined) {
        if (request.userId.isDefined) {
          // Remove already seen ids from user history
          val unseenIds = resultIds -- RedisCacheClient.smembers(KeyUtils.postSeen(request.userId.get)).map(_.toLong)
          // Update user history
          if (unseenIds.nonEmpty) RedisCacheClient.sadd(KeyUtils.postSeen(request.userId.get), unseenIds.toSeq.map(_.toString))

          unseenIds
        } else {
          resultIds
        }
      } else {
        // Initial home feeds request
        if (request.userId.isDefined) {
          // Clear and initialize history cache
          RedisCacheClient.del(KeyUtils.postSeen(request.userId.get))
          RedisCacheClient.sadd(KeyUtils.postSeen(request.userId.get), results.flatten.map(_.toString))
        }
        resultIds
      }

      postService.getPostsByIds(resultIds).flatMap { list =>
        // Handle empty results
        if (list.isEmpty) {
          Future.successful(success(Messages("success.found"),
            Json.obj(
              "posts" -> Json.arr(),
              "next" -> ts.map(_ => DateTime.now.minusHours(3).getMillis / 1000).mkString(",")
            )))
        } else {
          val idTsMap = list.map(x => (x._1.id.get, x._1.created)).toMap
          val nextTs = results.map { rs =>
            if (rs.isEmpty) ""
            else idTsMap.get(rs.last).map(_.toString).getOrElse("")
          }.mkString(",")

          val promotePosts = list.filter(p => ads.contains(p._1.id.get))
          val postList = list.filter(p => uniqueIds.contains(p._1.id.get) && !ads.contains(p._1.id.get)).sortBy(-_._1.created).toList

          val posts = if (promotePosts.nonEmpty) HelperUtils.insertAt(promotePosts.head, HelperUtils.random(3, 3), postList) else postList

          // Filter user who is in blacklist
          val userBlacklist = RedisCacheClient.smembers(KeyUtils.userBlacklist).map(_.toLong)

          val futures = posts.filterNot(p => userBlacklist.contains(p._1.userId)).map { row =>
            val markIds = row._3.map(_._1)
            markService.checkLikes(request.userId.getOrElse(-1L), markIds).map { likedMarks =>
              val marksJson = row._3.sortBy(-_._3).map { fields =>
                Json.obj(
                  "mark_id" -> fields._1,
                  "tag" -> fields._2,
                  "likes" -> fields._3,
                  "is_liked" -> likedMarks.contains(fields._1)
                )
              }
              val post = row._1
              val user = row._2
              Json.obj(
                "post_id" -> post.id.get,
                "type" -> post.`type`,
                "content" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                "created" -> post.created,
                "user" -> Json.obj(
                  "user_id" -> user.identify,
                  "nickname" -> user.nickname,
                  "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
                  "likes" -> user.likes
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
    } // futureIds end

  }

}
