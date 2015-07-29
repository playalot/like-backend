package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import com.likeorz.services.NotificationService
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
      success(Messages("success.found"), Json.obj("count" -> count))
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
              "avatar" -> QiniuUtil.getAvatar(row._2.avatar, "small"),
              "likes" -> row._2.likes
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
              "avatar" -> QiniuUtil.getAvatar(row._2.avatar, "small"),
              "likes" -> row._2.likes
            ),
            "posts" -> Json.arr(row._3.map(postAndUser => Json.obj(
              "post_id" -> postAndUser._1.id.get,
              "content" -> QiniuUtil.getPhoto(postAndUser._1.content, "small"),
              "user" -> Json.obj(
                "user_id" -> postAndUser._2.identify,
                "nickname" -> postAndUser._2.nickname,
                "avatar" -> QiniuUtil.getAvatar(postAndUser._2.avatar, "small"),
                "likes" -> postAndUser._2.likes
              )
            ))),
            "post" -> row._3.map(postAndUser => Json.obj(
              "post_id" -> postAndUser._1.id.get,
              "content" -> QiniuUtil.getPhoto(postAndUser._1.content, "small"),
              "user" -> Json.obj(
                "user_id" -> postAndUser._2.identify,
                "nickname" -> postAndUser._2.nickname,
                "avatar" -> QiniuUtil.getAvatar(postAndUser._2.avatar, "small"),
                "likes" -> postAndUser._2.likes
              )
            )),
            "tag" -> row._1.tagName,
            "mark_id" -> row._1.markId,
            "timestamp" -> row._1.updated
          )
        }
      }
      success(Messages("success.found"), Json.obj(
        "notifications" -> Json.toJson(json),
        "ts" -> ts
      ))
    }
  }
}
