package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.Action
import com.likeorz.models._
import com.likeorz.services._
import services.PushService
import utils.QiniuUtil

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class CommentController @Inject() (
    val messagesApi: MessagesApi,
    markService: MarkService,
    userService: UserService,
    infoService: InfoService,
    notificationService: NotificationService,
    pushService: PushService) extends BaseController {

  def commentMark(id: Long) = SecuredAction.async(parse.json) { implicit request =>
    markService.getMark(id).flatMap {
      case Some(mark) =>
        val replyId = (request.body \ "reply_id").asOpt[Long]
        val content = (request.body \ "content").as[String].trim
        val place = (request.body \ "place").asOpt[String]
        val created = System.currentTimeMillis / 1000
        markService.commentMark(id, Comment(None, id, request.userId, replyId, content, created, place)).map { comment =>
          for {
            nickname <- userService.getNickname(request.userId)
            rowOpt <- markService.getMarkWithPost(id)
          } yield {
            rowOpt match {
              case Some(row) =>
                val (mark, post) = row
                if (comment.replyId.isDefined) {
                  // Notify replied user in DB
                  val notifyReplier = Notification(None, "REPLY", comment.replyId.get, comment.userId, System.currentTimeMillis / 1000, mark.tagName, post.id, mark.id)
                  for {
                    notify <- notificationService.insert(notifyReplier)
                    count <- notificationService.countForUser(comment.replyId.get)
                  } yield {
                    // Send push notification
                    pushService.sendPushNotificationToUser(comment.replyId.get, Messages("notification.reply", nickname, mark.tagName.getOrElse("")), count)
                  }
                } else {
                  if (mark.userId != request.userId) {
                    val notifyMarkUser = Notification(None, "COMMENT", mark.userId, comment.userId, System.currentTimeMillis / 1000, mark.tagName, post.id, mark.id)
                    for {
                      notify <- notificationService.insert(notifyMarkUser)
                      count <- notificationService.countForUser(mark.userId)
                    } yield {
                      // Send push notification
                      pushService.sendPushNotificationToUser(mark.userId, Messages("notification.comment", nickname, mark.tagName.getOrElse("")), count)
                    }
                  }
                  if (post.userId != mark.userId && post.userId != comment.userId) {
                    val notifyPostUser = Notification(None, "COMMENT", post.userId, comment.userId, System.currentTimeMillis / 1000, mark.tagName, post.id, mark.id)
                    for {
                      notify <- notificationService.insert(notifyPostUser)
                      count <- notificationService.countForUser(post.userId)
                    } yield {
                      // Send push notification
                      pushService.sendPushNotificationToUser(post.userId, Messages("notification.comment", nickname, mark.tagName.getOrElse("")), count)
                    }
                  }
                }
              case None =>
            }
          }
          success(Messages("success.comment", Json.obj("comment_id" -> comment.id)))
        }
      case None =>
        Future.successful(error(4022, Messages("invalid.markId")))
    }
  }

  def deleteCommentFromMark(commentId: Long) = SecuredAction.async { implicit request =>
    markService.deleteCommentFromMark(commentId, request.userId).map {
      case true  => success(Messages("success.deleteComment"))
      case false => error(4029, Messages("failed.deleteComment"))
    }
  }

  def getCommentsForMark(markId: Long, order: String) = Action.async { implicit request =>
    markService.getCommentsForMark(markId, order).map { results =>
      val totalComments = results.length
      val commentsJson = results.map { row =>
        Json.obj(
          "comment_id" -> row._1.id,
          "content" -> row._1.content,
          "created" -> row._1.created,
          "place" -> row._1.place,
          "user" -> Json.obj(
            "user_id" -> row._2.id,
            "nickname" -> row._2.nickname,
            "avatar" -> QiniuUtil.getAvatar(row._2.avatar, "small"),
            "likes" -> row._2.likes
          ),
          "reply" -> row._3.map(reply => Json.obj(
            "user_id" -> reply.id,
            "nickname" -> reply.nickname,
            "avatar" -> QiniuUtil.getAvatar(reply.avatar, "small"),
            "likes" -> reply.likes
          ))
        )
      }
      success(Messages("success.found"), Json.obj(
        "total_comments" -> totalComments,
        "comments" -> Json.toJson(commentsJson)
      ))
    }
  }

}
