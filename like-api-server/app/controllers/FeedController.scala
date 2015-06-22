package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import services.{ PostService, MarkService }
import utils.{ RedisCacheClient, QiniuUtil }

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

    // Parse timestamp used for each data source
    val times = if (timestamp.isDefined) {
      val tsArr = timestamp.get.split(",").map(t => Some(t.toLong)).toSeq
      if (tsArr.length < 4) tsArr ++ Seq.fill(4 - tsArr.length)(None) else tsArr
    } else Seq(None, None, None, None)

    // Get post ids from different data source
    val futureIds = if (request.userId.isDefined) {
      val recommendIds = postService.getRecommendedPosts(9, times(0))
      val recentIds = postService.getRecentPosts(4, times(1))
      val followIds = postService.getFollowingPosts(request.userId.get, 6, times(2))
      val taggedIds = postService.getTaggedPosts(request.userId.get, 5, times(3))
      Future.sequence(Seq(recommendIds, recentIds, followIds, taggedIds))
    } else {
      val recommendIds = postService.getRecommendedPosts(13, times(0))
      val recentIds = postService.getRecentPosts(5, times(0))
      Future.sequence(Seq(recommendIds, recentIds))
    }

    futureIds.flatMap { results =>
      val resultIds = results.flatten.toSet
      // Unique result post ids
      val uniqueIds = if (timestamp.isDefined) { // Page feeds
        if (request.userId.isDefined) {
          // Remove duplicate ids from user history
          val diffIds = resultIds -- RedisCacheClient.sMembers("posts_seen:" + request.userId.get).map(_.toLong)
          // Update user history
          if (diffIds.nonEmpty) RedisCacheClient.sAdd("posts_seen:" + request.userId.get, diffIds.map(_.toString).toSeq: _*)
          diffIds
        } else {
          resultIds
        }
      } else { // Homepage top feeds
        if (request.userId.isDefined) {
          // Clear and initialize history cache
          RedisCacheClient.del("posts_seen:" + request.userId.get)
          RedisCacheClient.sAdd("posts_seen:" + request.userId.get, results.flatten.map(_.toString): _*)
        }
        resultIds
      }

      postService.getPostsByIds(resultIds).flatMap { list =>
        val idTsMap = list.map(x => (x._1.id.get, x._1.created)).toMap
        val nextTs = results.map(rs => idTsMap(rs.last)).mkString(",")
        val futures = list.filter(p => uniqueIds.contains(p._1.id.get)).map { row =>
          val markIds = row._2.map(_._1)
          markService.checkLikes(request.userId.getOrElse(-1L), markIds).map { likedMarks =>
            val marksJson = row._2.map { fields =>
              Json.obj(
                "mark_id" -> fields._1,
                "tag" -> fields._2,
                "likes" -> fields._3,
                "is_liked" -> likedMarks.contains(fields._1)
              )
            }
            val post = row._1
            Json.obj(
              "post_id" -> post.id.get,
              "type" -> post.`type`,
              "content" -> QiniuUtil.getPhoto(post.content, "medium"),
              "created" -> post.created,
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
