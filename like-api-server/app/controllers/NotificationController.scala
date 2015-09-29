package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.{ JsObject, Json }
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

  @deprecated("Old notification", "v1.1.1")
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

  def getNotificationsV2(ts: Option[Long] = None) = SecuredAction.async { implicit request =>

    val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)

    notificationService.getNotifications(request.userId, ts).map { results =>
      val ts = results.lastOption.map(_._1.updated)

      val combinedResults = results.foldLeft(scala.collection.mutable.MutableList[JsObject]()) { (jsonArr, a) =>
        val (notification, user, postOpt) = a
        if (notification.`type` == "FOLLOW") {
          jsonArr += Json.obj(
            "type" -> notification.`type`,
            "user" -> Json.obj(
              "user_id" -> user.id,
              "nickname" -> user.nickname,
              "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
              "likes" -> user.likes
            ),
            "timestamp" -> notification.updated
          )
        } else {
          if (notification.`type` == "LIKE"
            && jsonArr.nonEmpty
            && (jsonArr.last \ "type").as[String].contains("LIKE")
            && notification.fromUserId == (jsonArr.last \ "user" \ "user_id").as[Long]
            && notification.postId.get == (jsonArr.last \ "post" \ "post_id").as[Long]) {

            val mergedJson = jsonArr.last.deepMerge(Json.obj(
              "tags" -> (jsonArr.last \ "tags").as[List[String]].+:(notification.tagName.get)
            ))
            jsonArr.update(jsonArr.length - 1, mergedJson)

          } else {
            jsonArr += Json.obj(
              "type" -> notification.`type`,
              "user" -> Json.obj(
                "user_id" -> user.id,
                "nickname" -> user.nickname,
                "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
                "likes" -> user.likes
              ),
              "post" -> postOpt.map { postAndUser =>
                val (post, user) = postAndUser
                Json.obj(
                  "post_id" -> post.id.get,
                  "thumbnail" -> QiniuUtil.getThumbnailImage(post.content),
                  "preview" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                  "user" -> Json.obj(
                    "user_id" -> user.id,
                    "nickname" -> user.nickname,
                    "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
                    "likes" -> user.likes
                  )
                )
              },
              "tags" -> notification.tagName.toSeq,
              "timestamp" -> notification.updated
            )
          }
        }
        jsonArr
      }
      success(Messages("success.found"), Json.obj(
        "notifications" -> Json.toJson(combinedResults),
        "ts" -> ts
      ))
    }
  }

}
