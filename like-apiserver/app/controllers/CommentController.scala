package controllers

import javax.inject.Inject

import models.Comment
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.Action
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
        markService.commentMark(id, Comment(None, id, request.userId, replyId, content, created, location)).map { comment =>
          Ok(Json.obj(
            "code" -> 1,
            "message" -> "Success",
            "data" -> Json.obj(
              "comment_id" -> comment.id
            )
          ))
        }
      case None =>
        Future.successful(Ok(Json.obj(
          "code" -> 4022,
          "message" -> Messages("mark.notFound")
        )))
    }
  }

  def deleteCommentFromMark(commentId: Long) = SecuredAction.async { implicit request =>
    markService.deleteCommentFromMark(commentId, request.userId).map {
      case true =>
        Ok(Json.obj(
          "code" -> 1,
          "message" -> "Delete Success"
        ))
      case false =>
        Ok(Json.obj(
          "code" -> 1,
          "message" -> "Delete Failed"
        ))
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

      Ok(Json.obj(
        "code" -> 1,
        "message" -> "Record(s) Found",
        "data" -> Json.obj(
          "total_comments" -> totalComments,
          "comments" -> Json.toJson(commentsJson)
        )
      ))
    }
  }

}
