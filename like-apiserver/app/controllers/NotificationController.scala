package controllers

import javax.inject.Inject

import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.NotificationService
import utils.QiniuUtil

/**
 * Created by Guan Guan
 * Date: 6/1/15
 */
class NotificationController @Inject() (
    val messagesApi: MessagesApi,
    notificationService: NotificationService) extends BaseController {

  def notificationCount = SecuredAction.async { implicit request =>
    notificationService.countForUser(request.userId).map { count =>
      Ok(Json.obj(
        "code" -> 1,
        "message" -> "Record(s) Found",
        "data" -> Json.obj(
          "count" -> count
        )
      ))
    }
  }

  def getNotifications(ts: Option[Long] = None) = SecuredAction.async { implicit request =>
    notificationService.getNotifications(request.userId, ts).map { results =>
      val ts = results.lastOption.map(_._1.updated)
      val json = results.map { row =>
        if (row._1.`type` == "FOLLOW") {
          Json.obj(
            "type" -> row._1.`type`,
            "user" -> Json.obj(
              "user_id" -> row._2.id.get.toString,
              "nickname" -> row._2.nickname,
              "avatar" -> QiniuUtil.getAvatar(row._2.avatar, "small")
            ),
            "posts" -> Json.arr(),
            "timestamp" -> row._1.updated
          )
        } else {
          Json.obj(
            "type" -> row._1.`type`,
            "user" -> Json.obj(
              "user_id" -> row._2.id.get.toString,
              "nickname" -> row._2.nickname,
              "avatar" -> QiniuUtil.getAvatar(row._2.avatar, "small")
            ),
            "posts" -> Json.arr(row._3.map(post => Json.obj(
              "post_id" -> post.id.get,
              "content" -> QiniuUtil.getPhoto(post.content, "small")
            ))),
            "post" -> row._3.map(post => Json.obj(
              "post_id" -> post.id.get,
              "content" -> QiniuUtil.getPhoto(post.content, "small")
            )),
            "tag" -> row._1.tagName,
            "timestamp" -> row._1.updated
          )
        }

      }
      Ok(Json.obj(
        "code" -> 1,
        "message" -> "Record(s) Found",
        "data" -> Json.obj(
          "notifications" -> Json.toJson(json),
          "ts" -> ts
        )
      ))
    }
  }
}
