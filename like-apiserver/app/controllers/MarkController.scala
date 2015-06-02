package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.MarkService
import utils.QiniuUtil

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/2/15
 */
class MarkController @Inject() (
    val messagesApi: MessagesApi,
    markService: MarkService) extends BaseController {

  def like(markId: Long) = SecuredAction.async { implicit request =>
    markService.getMark(markId).flatMap {
      case Some(mark) =>
        markService.like(markId, request.userId).map { _ =>
          Ok(Json.obj(
            "code" -> 1,
            "message" -> "Like Success"
          ))
        }
      case None =>
        Future.successful(Ok(Json.obj(
          "code" -> 4022,
          "message" -> Messages("mark.notFound")
        )))
    }
  }

  def unlike(markId: Long) = SecuredAction.async { implicit request =>
    markService.getMark(markId).flatMap {
      case Some(mark) =>
        markService.unlike(markId, request.userId).map { _ =>
          Ok(Json.obj(
            "code" -> 1,
            "message" -> "Unlike Success"
          ))
        }
      case None =>
        Future.successful(Ok(Json.obj(
          "code" -> 4022,
          "message" -> Messages("mark.notFound")
        )))
    }
  }

  def getLikes(markId: Long) = SecuredAction.async { implicit request =>
    markService.getMark(markId).flatMap {
      case Some(mark) =>
        markService.getLikes(markId).map { rows =>
          val json = rows.map { row =>
            val (like, user) = row
            Json.obj(
              "user" -> Json.obj(
                "user_id" -> user.id.get.toString,
                "nickname" -> user.nickname,
                "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
                "likes" -> user.likes
              )
            )
          }
          Ok(Json.obj(
            "code" -> 1,
            "message" -> "Record(s) Found",
            "data" -> Json.obj(
              "likes" -> Json.toJson(json)
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

}
