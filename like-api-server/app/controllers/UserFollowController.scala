package controllers

import javax.inject.Inject

import com.likeorz.models.Notification
import com.likeorz.push.JPushNotification
import com.likeorz.services.{ UserService, UserFollowService, NotificationService }
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import services.PushService
import utils.QiniuUtil

import scala.concurrent.Future

class UserFollowController @Inject() (
    val messagesApi: MessagesApi,
    userFollowService: UserFollowService,
    userService: UserService,
    notificationService: NotificationService,
    pushService: PushService) extends BaseController {

  def getFollowing(id: Long, page: Int) = Action.async {
    userFollowService.getFollowings(id, page).map { list =>
      val jsonArr = list.map { user =>
        Json.obj(
          "user_id" -> user.id.toString,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes
        )
      }
      success(Messages("success.found"), Json.obj("follows" -> Json.toJson(jsonArr)))
    }
  }

  def getFollowers(id: Long, page: Int) = Action.async {
    userFollowService.getFollowers(id, page).map { list =>
      val jsonArr = list.map { result =>
        val (user, isFollowing) = result
        Json.obj(
          "user_id" -> user.id.toString,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes,
          "is_following" -> isFollowing
        )
      }
      success(Messages("success.found"), Json.obj("fans" -> Json.toJson(jsonArr)))
    }
  }

  def follow(id: Long) = (SecuredAction andThen BannedUserCheckAction).async { implicit request =>
    if (id == request.userId) {
      Future.successful(error(4017, Messages("failed.followYourself")))
    } else {
      userService.findById(id).flatMap {
        case Some(user) =>
          for {
            nickname <- userService.getNickname(request.userId)
            following <- userFollowService.follow(request.userId, id)
          } yield {
            val notifyFollow = Notification(None, "FOLLOW", id, request.userId, System.currentTimeMillis / 1000, None, None)
            for {
              notify <- notificationService.insert(notifyFollow)
              count <- notificationService.countForUser(id)
            } yield {
              // Send push notification
              pushService.sendPushNotificationViaJPush(JPushNotification(List(id.toString), List(), Messages("notification.follow", nickname), count))
              pushService.sendPushNotificationToUser(id, Messages("notification.follow", nickname), count)
            }
            success(Messages("success.follow"), Json.obj("is_following" -> following))
          }
        case None => Future.successful(error(4022, Messages("invalid.userId")))
      }
    }
  }

  def unFollow(id: Long) = SecuredAction.async { implicit request =>
    if (id == request.userId) {
      Future.successful(error(4018, Messages("failed.unFollowYourself")))
    } else {
      userService.findById(id).flatMap {
        case Some(user) =>
          userFollowService.unFollow(request.userId, id).map { following =>
            success(Messages("success.unFollow"), Json.obj("is_following" -> 0))
          }
        case None =>
          Future.successful(error(4022, Messages("invalid.userId")))
      }
    }
  }

}
