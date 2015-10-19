package controllers

import javax.inject.Inject

import com.likeorz.event.{ LikeEventType, LikeEvent }
import com.likeorz.models.Notification
import com.likeorz.services.store.MongoDBService
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import com.likeorz.services.{ EventBusService, UserService, NotificationService, MarkService }
import utils.QiniuUtil

import scala.concurrent.Future

class MarkController @Inject() (
    val messagesApi: MessagesApi,
    markService: MarkService,
    userService: UserService,
    eventBusService: EventBusService,
    mongoDBService: MongoDBService,
    notificationService: NotificationService) extends BaseController {

  def like(markId: Long) = (SecuredAction andThen BannedUserCheckAction).async { implicit request =>
    markService.getMarkWithPostAuthor(markId).flatMap {
      case Some((mark, postAuthor)) =>
        // log event
        eventBusService.publish(LikeEvent(None, LikeEventType.like, "user", request.userId.toString, Some("mark"), Some(markId.toString), properties = Json.obj("tag" -> mark.tagName)))
        markService.like(mark, postAuthor, request.userId).map { _ =>
          // Add like to mongodb
          mongoDBService.likeMark(markId, mark.postId, request.userId)

          if (mark.userId != request.userId) {
            val notifyMarkUser = Notification(None, "LIKE", mark.userId, request.userId, System.currentTimeMillis / 1000, mark.tagName, Some(mark.postId))
            notificationService.insert(notifyMarkUser)
          }
          if (postAuthor != mark.userId && postAuthor != request.userId) {
            val notifyPostUser = Notification(None, "LIKE", postAuthor, request.userId, System.currentTimeMillis / 1000, mark.tagName, Some(mark.postId))
            notificationService.insert(notifyPostUser)
          }
          success(Messages("success.like"))
        }
      case None =>
        Future.successful(error(4022, Messages("invalid.markId")))
    }
  }

  def unlike(markId: Long) = SecuredAction.async { implicit request =>
    markService.getMarkWithPostAuthor(markId).flatMap {
      case Some((mark, postAuthor)) =>
        markService.unlike(mark, postAuthor, request.userId).map { rs =>
          // Remove like from mongodb
          if (rs == 1) {
            mongoDBService.deleteMarkForPost(markId, mark.postId)
          } else {
            mongoDBService.unlikeMark(markId, mark.postId, request.userId)
          }

          notificationService.deleteLikeNotification(request.userId, mark.postId, mark.tagName.getOrElse(""))
          success(Messages("success.unLike"))
        }
      case None =>
        Future.successful(error(4022, Messages("invalid.markId")))
    }
  }

  def getLikes(markId: Long) = Action.async { implicit request =>
    markService.getMark(markId).flatMap {
      case Some(mark) =>
        markService.getLikes(markId).map { rows =>
          val json = rows.map { row =>
            val user = row._2
            Json.obj(
              "user" -> Json.obj(
                "user_id" -> user.id.toString,
                "nickname" -> user.nickname,
                "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
                "likes" -> user.likes
              )
            )
          }
          success(Messages("success.found"), Json.obj("likes" -> Json.toJson(json)))
        }
      case None => Future.successful(error(4022, Messages("invalid.markId")))
    }
  }

  def getMark(markId: Long) = UserAwareAction.async { implicit request =>
    markService.getMarkWithLikes(markId).flatMap {
      case Some(markAndlikes) =>
        val (mark, likes) = markAndlikes
        for {
          user <- userService.getUserInfo(mark.userId)
          isLiked <- { if (request.userId.isDefined) markService.isLikedByUser(markId, request.userId.get) else Future.successful(false) }
        } yield {
          val json = Json.obj(
            "mark_id" -> mark.identify,
            "user" -> Json.obj(
              "user_id" -> mark.userId,
              "nickname" -> user.nickname,
              "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
              "likes" -> user.likes
            ),
            "tag" -> mark.tagName,
            "likes" -> likes,
            "is_liked" -> isLiked,
            "created" -> mark.created
          )
          success(Messages("success.found"), json)
        }
      case None => Future.successful(error(4022, Messages("invalid.markId")))
    }
  }

  def deleteMark(markId: Long) = SecuredAction.async { implicit request =>
    markService.getMarkWithPost(markId).flatMap {
      case Some((mark, post)) =>
        if (request.userId == mark.userId || request.userId == post.userId) {
          markService.deleteMark(markId).map { _ =>
            // Remove mark from mongodb
            mongoDBService.deleteMarkForPost(markId, post.id.get)
            // Remove related post from timeline
            eventBusService.publish(LikeEvent(None, LikeEventType.removeMark, "user", request.userId.toString, Some("post"), Some(post.identify), properties = Json.obj("tag" -> mark.tagName.get)))
            success(Messages("success.deleteMark"))
          }
        } else {
          Future.successful(error(4023, Messages("no.permission")))
        }
      case None => Future.successful(error(4022, Messages("invalid.markId")))
    }
  }

}
