package controllers

import javax.inject.Inject

import models.Comment
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.Action
import services.TagService
import utils.QiniuUtil

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class CommentController @Inject() (
    val messagesApi: MessagesApi,
    tagService: TagService) extends BaseController {

  def commentMark(id: Long) = SecuredAction.async(parse.json) { implicit request =>

    val replyId = (request.body \ "reply_id").asOpt[Long]
    val content = (request.body \ "content").as[String].trim
    val location = (request.body \ "location").asOpt[String]
    val created = System.currentTimeMillis / 1000

    tagService.commentMark(Comment(None, id, request.userId, replyId, content, created, location)).map { comment =>
      Ok(Json.obj(
        "code" -> 1,
        "message" -> "Success",
        "data" -> Json.obj(
          "comment_id" -> comment.id
        )
      ))
    }
  }

  def deleteCommentFromMark(commentId: Long) = SecuredAction.async { implicit request =>
    tagService.deleteCommentFromMark(commentId, request.userId).map {
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

    tagService.getCommentsForMark(markId).map { results =>
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
