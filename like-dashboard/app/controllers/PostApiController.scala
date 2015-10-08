package controllers

import javax.inject.Inject

import com.likeorz.event.{ LikeEventType, LikeEvent }
import com.likeorz.models.{ Notification, MarkDetail }
import com.likeorz.push.JPushNotification
import com.likeorz.services.store.MongoDBService
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.Admin
import play.api.Configuration
import play.api.i18n.{ Messages, MessagesApi }
import com.likeorz.services._
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.{ AdminService, DashboardService }
import utils.QiniuUtil

import scala.concurrent.Future

class PostApiController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[Admin, CookieAuthenticator],
    eventBusService: EventBusService,
    mongoDBService: MongoDBService,
    adminService: AdminService,
    dashboardService: DashboardService,
    userService: UserService,
    postService: PostService,
    markService: MarkService,
    pushService: PushService,
    notificationService: NotificationService,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    configuration: Configuration) extends Silhouette[Admin, CookieAuthenticator] {

  def fetchPostList(timestamp: Option[Long], filter: Option[String]) = SecuredAction.async { implicit request =>
    // Use phone screen width for output photo size
    val screenWidth = 600
    val pageSize = 48

    postService.getRecentPosts(pageSize, timestamp, filter).flatMap { ids =>
      if (ids.isEmpty) {
        Future.successful(Ok(Json.obj("posts" -> Json.arr())))
      } else {
        postService.getPostsByIdsSimple(ids).flatMap { posts =>
          if (posts.isEmpty) {
            Future.successful(Ok(Json.obj("posts" -> Json.arr())))
          } else {

            val marksMap = mongoDBService.getPostMarksByIds(ids)

            val sortedPosts = posts.sortBy(-_.created)

            val futures = sortedPosts.map { post =>

              for {
                isRecommended <- dashboardService.isPostRecommended(post.id.get)
                isBlocked <- dashboardService.isPostBlocked(post.id.get)
              } yield {
                val marks = marksMap.getOrElse(post.id.get, Seq())
                val userInfo = userService.getUserInfoFromCache(post.userId)
                val marksJson = marks.map { mark =>
                  Json.obj(
                    "markId" -> mark.markId,
                    "tag" -> mark.tag,
                    "likes" -> mark.likes.size,
                    "likedBy" -> Json.toJson(mark.likes)
                  )
                }
                Json.obj(
                  "id" -> post.id.get,
                  "type" -> post.`type`,
                  "content" -> QiniuUtil.squareImage(post.content, screenWidth),
                  "created" -> post.created,
                  "isRecommended" -> isRecommended,
                  "isBlocked" -> isBlocked,
                  "user" -> Json.obj(
                    "userId" -> post.userId,
                    "nickname" -> userInfo.nickname,
                    "avatar" -> QiniuUtil.resizeImage(userInfo.avatar, 50),
                    "likes" -> userInfo.likes
                  ),
                  "marks" -> Json.toJson(marksJson)
                )
              }
            }
            Future.sequence(futures).map { posts =>
              Ok(Json.obj("posts" -> Json.toJson(posts), "next" -> sortedPosts.last.created))
            }
          }
        }
      }
    }
  }

  def recommendPost(postId: Long, status: Boolean) = SecuredAction.async {
    dashboardService.recommendPost(postId, status).map { rs =>
      eventBusService.publish(LikeEvent(None, LikeEventType.recommendToAll, "post", postId.toString))
      Ok
    }
  }

  def invisiblePost(postId: Long, status: Boolean) = SecuredAction.async {
    dashboardService.blockPost(postId, status).map(_ => Ok)
  }

  def isPostRecommended(postId: Long) = SecuredAction.async {
    dashboardService.isPostRecommended(postId).map(x => Ok(Json.obj("status" -> x)))
  }

  def isPostInvisible(postId: Long) = SecuredAction.async {
    dashboardService.isPostBlocked(postId).map(x => Ok(Json.obj("status" -> x)))
  }

  def deleteMark(markId: Long) = SecuredAction.async {
    markService.getMark(markId).flatMap {
      case Some(mark) =>
        markService.deleteMark(markId).map { _ =>
          mongoDBService.deleteMarkForPost(markId, mark.postId)
          eventBusService.publish(LikeEvent(None, LikeEventType.removeMark, "user", "", Some("post"), Some(mark.postId.toString), properties = Json.obj("tag" -> mark.tagName.get)))
          Ok("success.deleteMark")
        }
      case None => Future.successful(Ok("success.deleteMark"))
    }
  }

  def addMark(postId: Long, tag: String, userId: Long) = SecuredAction.async {
    if (tag.trim.length > 13)
      Future.successful(BadRequest("tag too long"))
    else if (tag.trim.length < 1)
      Future.successful(BadRequest("tag too short"))
    else
      postService.getPostById(postId).flatMap {
        case Some(post) =>
          // mark event
          eventBusService.publish(LikeEvent(None, LikeEventType.mark, "user", userId.toString, Some("post"), Some(postId.toString), properties = Json.obj("tag" -> tag)))
          for {
            nickname <- userService.getNickname(userId)
            mark <- postService.addMark(postId, post.userId, tag, userId)
            update <- postService.updatePostTimestamp(postId)
          } yield {
            // Insert mark into mongodb
            mongoDBService.insertMarkForPost(postId, MarkDetail(mark.id.get, userId, mark.tagId, tag, Seq(userId)))

            if (userId != post.userId) {
              val notifyMarkUser = Notification(None, "MARK", post.userId, userId, System.currentTimeMillis / 1000, Some(tag), Some(postId))
              for {
                notify <- notificationService.insert(notifyMarkUser)
                count <- notificationService.countForUser(post.userId)
              } yield {
                // Send push notification
                pushService.sendPushNotificationViaJPush(JPushNotification(List(post.userId.toString), List(), Messages("notification.mark", nickname, tag), count))
                pushService.sendPushNotificationToUser(post.userId, Messages("notification.mark", nickname, tag), count)
              }
            }
            Ok(Json.obj(
              "markId" -> mark.id.get,
              "tag" -> tag,
              "likes" -> 1,
              "likedBy" -> Json.arr(userId)
            ))
          }
        case None => Future.successful(BadRequest("post not found"))
      }
  }

  def like(markId: Long, userId: Long) = SecuredAction.async {
    markService.getMarkWithPostAuthor(markId).flatMap {
      case Some((mark, postAuthor)) =>
        // log event
        eventBusService.publish(LikeEvent(None, "like", "user", userId.toString, Some("mark"), Some(markId.toString), properties = Json.obj("tag" -> mark.tagName)))
        markService.like(mark, postAuthor, userId).map { _ =>
          // Add like to mongodb
          mongoDBService.likeMark(markId, mark.postId, userId)
          if (mark.userId != userId) {
            val notifyMarkUser = Notification(None, "LIKE", mark.userId, userId, System.currentTimeMillis / 1000, mark.tagName, Some(mark.postId))
            notificationService.insert(notifyMarkUser)
          }
          if (postAuthor != mark.userId && postAuthor != userId) {
            val notifyPostUser = Notification(None, "LIKE", postAuthor, userId, System.currentTimeMillis / 1000, mark.tagName, Some(mark.postId))
            notificationService.insert(notifyPostUser)
          }
          Ok("success.like")
        }
      case None =>
        Future.successful(BadRequest("invalid.markId"))
    }
  }

  def unlike(markId: Long, userId: Long) = SecuredAction.async {
    markService.getMarkWithPostAuthor(markId).flatMap {
      case Some((mark, postAuthor)) =>
        markService.unlike(mark, postAuthor, userId).map { rs =>
          // Remove like from mongodb
          if (rs == 1) {
            mongoDBService.deleteMarkForPost(markId, mark.postId)
          } else {
            mongoDBService.unlikeMark(markId, mark.postId, userId)
          }
          notificationService.deleteLikeNotification(userId, mark.postId, mark.tagName.getOrElse(""))
          Ok("success.unLike")
        }
      case None =>
        Future.successful(BadRequest("invalid.markId"))
    }
  }

  def deletePost(postId: Long) = SecuredAction.async {
    postService.getPostById(postId).flatMap {
      case Some(post) =>
        for {
          p <- postService.deletePostById(postId, post.userId)
          n <- notificationService.deleteAllNotificationForPost(postId)
          r <- postService.recordDelete(post.content)
        } yield {
          mongoDBService.deletePostMarks(postId: Long)
          Ok("success.deletePost")
        }
      case None => Future.successful(NotFound)
    }
  }

  def countPostLikes(tag: String) = SecuredAction.async {
    dashboardService.countPostTotalLikes(tag).map { scores =>
      val jsonArr = scores.map(s => Json.obj("post_id" -> s._1, "total_likes" -> s._2))
      Ok(Json.toJson(jsonArr))
    }
  }

  def judgePost(postId: Long, judge: Long) = SecuredAction.async {
    println(postId)
    println(judge)
    dashboardService.judgePost(postId, judge == 1).map(_ => Ok)
  }

  def getNotJudgedPosts(startId: Option[Long]) = SecuredAction.async {
    dashboardService.getNotJudgedPosts(startId).map { results =>
      Ok(Json.toJson(
        results.map(rs => Json.obj("id" -> rs._1, "image" -> QiniuUtil.resizeImage(rs._2, 600, isParsedFilename = true)))
      ))
    }
  }

}
