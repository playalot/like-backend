package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
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
        markService.like(markId, request.userId).map(_ => success(Messages("success.like")))
      case None =>
        Future.successful(error(4022, Messages("invalid.markId")))
    }
  }

  def unlike(markId: Long) = SecuredAction.async { implicit request =>
    markService.getMark(markId).flatMap {
      case Some(mark) =>
        markService.unlike(markId, request.userId).map(_ => success(Messages("success.unLike")))
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

}
