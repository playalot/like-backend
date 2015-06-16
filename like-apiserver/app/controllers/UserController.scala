package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.{ MarkService, PostService, UserService }
import utils.QiniuUtil

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/23/15
 */
class UserController @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService,
    markService: MarkService,
    postService: PostService) extends BaseController {

  def getInfo(id: Long) = UserAwareAction.async { implicit request =>
    userService.findById(id).flatMap {
      case Some(user) =>
        for {
          countFollowers <- userService.countFollowers(user.id.get)
          countFriends <- userService.countFriends(id)
          countPosts <- postService.countByUserId(id)
          isFollowing <- if (request.userId.isDefined) userService.isFollowing(request.userId.get, id) else Future.successful(0)
        } yield {
          success(Messages("success.recordFound"), Json.obj(
            "user_id" -> id.toString,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.getAvatar(user.avatar, "large"),
            "origin_avatar" -> QiniuUtil.getAvatar(user.avatar, "origin"),
            "cover" -> QiniuUtil.getSizedImage(user.cover, getScreenWidth),
            "likes" -> user.likes,
            "is_following" -> isFollowing,
            "count" -> Json.obj(
              "post" -> countPosts,
              "follow" -> countFriends,
              "fan" -> countFriends
            )
          ))
        }
      case None => Future.successful(error(4014, Messages("invalid.userId")))
    }
  }

  def updateNickname() = SecuredAction.async(parse.json) { implicit request =>
    (request.body \ "nickname").asOpt[String] match {
      case Some(nickname) =>
        userService.nicknameExists(nickname.trim).flatMap {
          case true => Future.successful(error(4040, Messages("invalid.nicknameExist")))
          case false =>
            if (nickname.trim.length > 20) {
              Future.successful(error(4040, Messages("invalid.nicknameLong")))
            } else if (nickname.trim.length < 2) {
              Future.successful(error(4040, Messages("invalid.nicknameShort")))
            } else {
              userService.updateNickname(request.userId, nickname.trim).map(_ => success(Messages("success.nickname")))
            }
        }
      case None => Future.successful(error(4040, Messages("invalid.nickname")))
    }
  }

  def updateAvatar() = SecuredAction.async(parse.json) { implicit request =>
    (request.body \ "avatar").asOpt[String] match {
      case Some(avatar) =>
        userService.updateAvatar(request.userId, avatar).map(_ => success(Messages("success.avatar")))
      case None =>
        Future.successful(error(4041, Messages("invalid.avatar")))
    }
  }

  def getPostsForUser(id: Long, page: Int) = SecuredAction.async { implicit request =>
    userService.findById(id).flatMap {
      case Some(user) =>
        postService.getPostsByUserId(id, page).flatMap { list =>
          val futures = list.map { row =>
            val markIds = row._2.map(_._1)
            markService.checkLikes(request.userId, markIds).map { likedMarks =>
              val marksJson = row._2.map { fields =>
                Json.obj(
                  "mark_id" -> fields._1,
                  "tag" -> fields._2,
                  "likes" -> fields._3,
                  "is_liked" -> likedMarks.contains(fields._1)
                )
              }
              val post = row._1
              Json.obj(
                "post_id" -> post.id.get,
                "type" -> post.`type`,
                "content" -> QiniuUtil.getPhoto(post.content, "medium"),
                "created" -> post.created,
                "marks" -> Json.toJson(marksJson)
              )
            }
          }
          Future.sequence(futures).map { posts =>
            success(Messages("success.recordFound"), Json.obj("posts" -> Json.toJson(posts)))
          }
        }
      case None =>
        Future.successful(error(4022, Messages("user.notFound")))
    }
  }

  def getFriends(id: Long, page: Int) = SecuredAction.async {
    userService.getFriends(id, page).map { list =>
      val jsonArr = list.map { user =>
        Json.obj(
          "user_id" -> user.identify,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes
        )
      }
      success(Messages("success.recordFound"), Json.obj("follows" -> Json.toJson(jsonArr)))
    }
  }

  def getFollowers(id: Long, page: Int) = SecuredAction.async {
    userService.getFollowers(id, page).map { list =>
      val jsonArr = list.map { user =>
        Json.obj(
          "user_id" -> user.identify,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes
        )
      }
      success(Messages("success.recordFound"), Json.obj("fans" -> Json.toJson(jsonArr)))
    }
  }

  def follow(id: Long) = SecuredAction.async { implicit request =>
    if (id == request.userId) {
      Future.successful(error(4017, Messages("forbid.followYourself")))
    } else {
      userService.findById(id).flatMap {
        case Some(user) =>
          userService.follow(request.userId, id).map { following =>
            Ok(Json.obj(
              "code" -> 1,
              "message" -> Messages("success.follow"),
              "data" -> Json.obj(
                "is_following" -> following
              )
            ))
          }
        case None =>
          Future.successful(error(4022, Messages("user.notFound")))
      }
    }
  }

}
