package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import com.likeorz.services.NotificationService
import utils.QiniuUtil

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
        val (notification, user, postOpt) = row

        if (notification.`type` == "FOLLOW") {
          Json.obj(
            "type" -> notification.`type`,
            "user" -> Json.obj(
              "user_id" -> user.id.toString,
              "nickname" -> user.nickname,
              "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
              "likes" -> user.likes
            ),
            "posts" -> Json.arr(),
            "timestamp" -> notification.updated
          )
        } else {
          Json.obj(
            "type" -> notification.`type`,
            "user" -> Json.obj(
              "user_id" -> user.id.toString,
              "nickname" -> user.nickname,
              "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
              "likes" -> user.likes
            ),
            "posts" -> Json.arr(postOpt.map(postAndUser => Json.obj(
              "post_id" -> postAndUser._1.id.get,
              "content" -> QiniuUtil.getPhoto(postAndUser._1.content, "small"),
              "user" -> Json.obj(
                "user_id" -> postAndUser._2.id.toString,
                "nickname" -> postAndUser._2.nickname,
                "avatar" -> QiniuUtil.getAvatar(postAndUser._2.avatar, "small"),
                "likes" -> postAndUser._2.likes
              )
            ))),
            "post" -> postOpt.map(postAndUser => Json.obj(
              "post_id" -> postAndUser._1.id.get,
              "content" -> QiniuUtil.getPhoto(postAndUser._1.content, "small"),
              "user" -> Json.obj(
                "user_id" -> postAndUser._2.id.toString,
                "nickname" -> postAndUser._2.nickname,
                "avatar" -> QiniuUtil.getAvatar(postAndUser._2.avatar, "small"),
                "likes" -> postAndUser._2.likes
              )
            )),
            "tag" -> notification.tagName,
            "mark_id" -> notification.markId,
            "timestamp" -> notification.updated
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
