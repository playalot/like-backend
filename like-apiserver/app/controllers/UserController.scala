package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.{ PostService, UserService }
import utils.QiniuUtil

import scala.concurrent.Future
import scala.concurrent.duration._

/**
 * Created by Guan Guan
 * Date: 5/23/15
 */
class UserController @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService, postService: PostService) extends BaseController {

  def getInfo(id: Long) = UserAwareAction.async { implicit request =>

    userService.findById(id).flatMap {
      case Some(user) =>
        for {
          countFollowers <- userService.countFollowers(user.id.get)
          countFriends <- userService.countFriends(id)
          //          countPosts <- postService.countByUserId(id)
          isFollowing <- if (request.userId.isDefined) userService.isFollowing(request.userId.get, id) else Future.successful(0)
        } yield {
          Ok(Json.obj(
            "code" -> 1,
            "message" -> "Record(s) Found",
            "data" -> Json.obj(
              "user_id" -> id.toString,
              "nickname" -> user.nickname,
              "avatar" -> QiniuUtil.getAvatar(user.avatar, "large"),
              "origin_avatar" -> QiniuUtil.getAvatar(user.avatar, "origin"),
              "cover" -> QiniuUtil.getSizedImage(user.cover, getScreenWidth),
              "likes" -> user.likes,
              "is_following" -> isFollowing,
              "count" -> Json.obj(
                "post" -> 0, //countPosts,
                "follow" -> countFriends,
                "fan" -> countFriends
              )
            )
          ))
        }
      case None => Future.successful(NotFound(Json.obj(
        "code" -> 4014,
        "field" -> "id",
        "message" -> Messages("invalid.userId")
      )))
    }
  }

  def updateNickname = SecuredAction.async(parse.json) { implicit request =>
    (request.body \ "nickname").asOpt[String] match {
      case Some(nickname) =>
        userService.nicknameExists(nickname).flatMap {
          case true => Future.successful(BadRequest(Json.obj(
            "code" -> 4040,
            "field" -> "nickname",
            "message" -> Messages("invalid.nicknameExist")
          )))
          case false =>
            if (nickname.length > 20) {
              Future.successful(BadRequest(Json.obj(
                "code" -> 4040,
                "field" -> "nickname",
                "message" -> Messages("invalid.nicknameLong")
              )))
            } else if (nickname.length < 2) {
              Future.successful(BadRequest(Json.obj(
                "code" -> 4040,
                "field" -> "nickname",
                "message" -> Messages("invalid.nicknameLong")
              )))
            } else {
              userService.updateNickname(request.userId, nickname).map(_ => Ok(Json.obj(
                "code" -> 1,
                "message" -> "Change Nickname Success"
              )))
            }
        }
      case None => Future.successful(BadRequest(Json.obj(
        "code" -> 4040,
        "field" -> "nickname",
        "message" -> Messages("invalid.nickname")
      )))
    }
  }

  def updateAvatar = SecuredAction.async(parse.json) { implicit request =>
    (request.body \ "avatar").asOpt[String] match {
      case Some(avatar) => {
        userService.updateAvatar(request.userId, avatar).map(_ => Ok(Json.obj(
          "code" -> 1,
          "message" -> Messages("success.avatar")
        )))
      }
      case None => Future.successful(BadRequest(Json.obj(
        "code" -> 4041,
        "field" -> "avatar",
        "message" -> Messages("invalid.avatar")
      )))
    }
  }

}
