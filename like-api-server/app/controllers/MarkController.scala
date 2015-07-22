package controllers

import javax.inject.{ Named, Inject }

import akka.actor.ActorRef
import com.likeorz.event.LikeEvent
import com.likeorz.models.Notification
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import services.{ NotificationService, MarkService }
import utils.QiniuUtil

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/2/15
 */
class MarkController @Inject() (
    @Named("event-producer-actor") eventProducerActor: ActorRef,
    val messagesApi: MessagesApi,
    markService: MarkService,
    notificationService: NotificationService) extends BaseController {

  def like(markId: Long) = SecuredAction.async { implicit request =>
    markService.getMarkWithPostAndTag(markId).flatMap {
      case Some((mark, post, tag)) =>
        // log event
        eventProducerActor ! LikeEvent(None, "like", "user", request.userId.toString, Some("mark"), Some(markId.toString), properties = Json.obj("tag" -> tag.tagName))
        markService.like(mark, post, request.userId).map { _ =>
          if (mark.userId != request.userId) {
            val notifyMarkUser = Notification(None, "LIKE", mark.userId, request.userId, System.currentTimeMillis / 1000, Some(tag.tagName), post.id)
            notificationService.insert(notifyMarkUser)
          }
          if (post.userId != mark.userId && post.userId != request.userId) {
            val notifyPostUser = Notification(None, "LIKE", post.userId, request.userId, System.currentTimeMillis / 1000, Some(tag.tagName), post.id)
            notificationService.insert(notifyPostUser)
          }
          success(Messages("success.like"))
        }
      case None =>
        Future.successful(error(4022, Messages("invalid.markId")))
    }
  }

  def unlike(markId: Long) = SecuredAction.async { implicit request =>
    markService.getMarkWithPostAndTag(markId).flatMap {
      case Some((mark, post, tag)) =>
        markService.unlike(mark, post, request.userId).map { _ =>
          notificationService.deleteLikeNotification(request.userId, post.id.get, tag.tagName)
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
                "user_id" -> user.id.get.toString,
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
    markService.getMarkWithUserAndLikes(markId, request.userId).map {
      case Some(row) =>
        val (mark, user, tag, likes, isLiked) = row
        val json = Json.obj(
          "mark_id" -> mark.identify,
          "user" -> Json.obj(
            "user_id" -> user.id.get.toString,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
            "likes" -> user.likes
          ),
          "tag" -> tag,
          "likes" -> likes,
          "is_liked" -> isLiked,
          "created" -> mark.created
        )
        success(Messages("success.found"), json)
      case None => error(4022, Messages("invalid.markId"))
    }
  }

}
