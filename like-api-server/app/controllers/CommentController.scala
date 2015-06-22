package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.Action
import com.likeorz.models._
import services.MarkService
import utils.QiniuUtil

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class CommentController @Inject() (
    val messagesApi: MessagesApi,
    markService: MarkService) extends BaseController {

  def commentMark(id: Long) = SecuredAction.async(parse.json) { implicit request =>
    markService.getMark(id).flatMap {
      case Some(mark) =>
        val replyId = (request.body \ "reply_id").asOpt[Long]
        val content = (request.body \ "content").as[String].trim
        val location = (request.body \ "location").asOpt[String]
        val created = System.currentTimeMillis / 1000
        markService.commentMark(id, Comment(None, id, request.userId, replyId, content, created, location))
          .map(comment => success(Messages("success.comment", Json.obj("comment_id" -> comment.id))))
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

  def getCommentsForMark(markId: Long) = Action.async { implicit request =>
    markService.getCommentsForMark(markId).map { results =>
      val totalComments = results.length
      val commentsJson = results.map { row =>
        Json.obj(
          "comment_id" -> row._1.id,
          "content" -> row._1.content,
          "created" -> row._1.created,
          "location" -> row._1.location,
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
