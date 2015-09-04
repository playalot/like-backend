package controllers

import javax.inject.Inject

import com.likeorz.models.Notification
import com.likeorz.push.JPushNotification
import play.api.Play
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import play.api.mvc.Action
import com.likeorz.services._
import services.PushService
import utils.{ HelperUtils, QiniuUtil }

import scala.concurrent.Future

class UserController @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService,
    tagService: TagService,
    markService: MarkService,
    postService: PostService,
    notificationService: NotificationService,
    pushService: PushService) extends BaseController {

  val DEFAULT_AVATAR = Play.configuration.getString("default.avatar").get
  val DEFAULT_COVER = Play.configuration.getString("default.cover").get

  def getInfo(id: Long) = UserAwareAction.async { implicit request =>
    userService.findById(id).flatMap {
      case Some(user) =>
        for {
          countFollowers <- userService.countFollowers(id)
          countFollowing <- userService.countFollowings(id)
          countPosts <- postService.countPostsForUser(id)
          countFavorite <- postService.countFavoriteForUser(id)
          countLikes <- markService.countLikesForUser(id)
          isFollowing <- if (request.userId.isDefined) userService.isFollowing(request.userId.get, id) else Future.successful(0)
        } yield {
          success(Messages("success.found"), Json.obj(
            "user_id" -> id.toString,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.getAvatar(user.avatar, "large"),
            "origin_avatar" -> QiniuUtil.getAvatar(user.avatar, "origin"),
            "cover" -> QiniuUtil.getSizedImage(user.cover, getScreenWidth),
            "likes" -> countLikes,
            "is_following" -> isFollowing,
            // Todo
            "count" -> Json.obj(
              "post" -> countPosts,
              "posts" -> countPosts,
              "follow" -> countFollowing,
              "following" -> countFollowing,
              "fan" -> countFollowers,
              "followers" -> countFollowers,
              "favorites" -> countFavorite
            )
          ))
        }
      case None => Future.successful(error(4014, Messages("invalid.userId")))
    }
  }

  def updateNickname() = (SecuredAction andThen BannedUserCheckAction).async(parse.json) { implicit request =>
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

  def updateAvatar() = (SecuredAction andThen BannedUserCheckAction).async(parse.json) { implicit request =>
    (request.body \ "avatar").asOpt[String] match {
      case Some(avatar) =>
        for {
          user <- userService.findById(request.userId)
          result <- userService.updateAvatar(request.userId, avatar)
        } yield {
          if (user.get.avatar != DEFAULT_AVATAR) {
            postService.recordDelete(user.get.avatar)
          }
          success(Messages("success.avatar"))
        }
      case None =>
        Future.successful(error(4041, Messages("invalid.avatar")))
    }
  }

  def updateCover() = (SecuredAction andThen BannedUserCheckAction).async(parse.json) { implicit request =>
    (request.body \ "cover").asOpt[String] match {
      case Some(avatar) =>
        for {
          user <- userService.findById(request.userId)
          result <- userService.updateCover(request.userId, avatar)
        } yield {
          if (user.get.cover != DEFAULT_COVER) {
            postService.recordDelete(user.get.cover)
          }
          success(Messages("success.cover"))
        }
      case None =>
        Future.successful(error(4041, Messages("invalid.cover")))
    }
  }

  def getPostsForUser(id: Long, page: Int) = UserAwareAction.async { implicit request =>
    userService.findById(id).flatMap {
      case Some(user) =>
        postService.getPostsByUserId(id, page, 21).flatMap { list =>
          val futures = list.map { row =>
            val markIds = row._2.map(_._1)
            markService.checkLikes(request.userId.getOrElse(-1L), markIds).map { likedMarks =>
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
            success(Messages("success.found"), Json.obj("posts" -> Json.toJson(posts)))
          }
        }
      case None =>
        Future.successful(error(4022, Messages("invalid.userId")))
    }
  }

  def getFavoritePostsForUser(ts: Option[Long]) = SecuredAction.async { implicit request =>
    postService.getFavoritePostsForUser(request.userId, 21, ts).flatMap { results =>
      val ids = results._1
      postService.getPostsByIds(ids).flatMap { list =>
        if (list.isEmpty) {
          Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
        } else {
          val sortedList = list.sortBy(-_._1.created)
          val futures = sortedList.map { row =>
            val post = row._1
            val markIds = row._2.map(_._1)

            for {
              userInfo <- userService.getUserInfo(post.userId)
              likedMarks <- markService.checkLikes(request.userId, markIds)
            } yield {
              val marksJson = row._2.sortBy(-_._3).map { fields =>
                Json.obj(
                  "mark_id" -> fields._1,
                  "tag" -> fields._2,
                  "likes" -> fields._3,
                  "is_liked" -> likedMarks.contains(fields._1)
                )
              }
              Json.obj(
                "post_id" -> post.id.get,
                "type" -> post.`type`,
                "content" -> QiniuUtil.getPhoto(post.content, "medium"),
                "created" -> post.created,
                "user" -> Json.obj(
                  "user_id" -> post.userId,
                  "nickname" -> userInfo.nickname,
                  "avatar" -> QiniuUtil.getAvatar(userInfo.avatar, "small"),
                  "likes" -> userInfo.likes
                ),
                "marks" -> Json.toJson(marksJson)
              )
            }
          }
          Future.sequence(futures).map { posts =>
            success(Messages("success.found"), Json.obj("posts" -> Json.toJson(posts), "next" -> results._2))
          }
        }
      }
    }
  }

  def suggestTags() = SecuredAction.async { implicit request =>
    tagService.suggestTagsForUser(request.userId).map {
      case (tags, recommends) =>
        val jsonArr = tags.map { tag =>
          Json.obj("tag" -> tag)
        }
        val recoJson = recommends.map { tag =>
          Json.obj("tag" -> tag)
        }
        success(Messages("success.found"), Json.obj("suggests" -> Json.toJson(jsonArr), "recommends" -> Json.toJson(recoJson), "defaults" -> Json.toJson(recoJson)))
    }
  }

  def getFriends(id: Long, page: Int) = Action.async {
    userService.getFollowings(id, page).map { list =>
      val jsonArr = list.map { user =>
        Json.obj(
          "user_id" -> user.identify,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes
        )
      }
      success(Messages("success.found"), Json.obj("follows" -> Json.toJson(jsonArr)))
    }
  }

  def getFollowers(id: Long, page: Int) = Action.async {
    userService.getFollowers(id, page).map { list =>
      val jsonArr = list.map { user =>
        Json.obj(
          "user_id" -> user.identify,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes
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
            following <- userService.follow(request.userId, id)
          } yield {
            val notifyFollow = Notification(None, "FOLLOW", id, request.userId, System.currentTimeMillis / 1000, None, None)
            for {
              notify <- notificationService.insert(notifyFollow)
              count <- notificationService.countForUser(id)
            } yield {
              // Send push notification
              if (HelperUtils.compareVersion(getLikeVersion(request.request), "1.1.1")) {
                pushService.sendPushNotificationViaJPush(JPushNotification(List(id.toString), List(), Messages("notification.follow", nickname), count))
              } else {
                pushService.sendPushNotificationToUser(id, Messages("notification.follow", nickname), count)
              }
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
          userService.unFollow(request.userId, id).map { following =>
            success(Messages("success.unFollow"), Json.obj("is_following" -> 0))
          }
        case None =>
          Future.successful(error(4022, Messages("invalid.userId")))
      }
    }
  }

  def block(id: Long) = SecuredAction.async { implicit request =>
    userService.block(request.userId, id).map { _ =>
      success(Messages("success.block"))
    }
  }

  def unBlock(id: Long) = SecuredAction.async { implicit request =>
    userService.unBlock(request.userId, id).map { _ =>
      success(Messages("success.unBlock"))
    }
  }

  def getUserTag(tagName: String) = SecuredAction.async { implicit request =>
    tagService.getTagByName(tagName).flatMap {
      case Some(tag) =>
        if (tag.group > 0) {
          tagService.getUserTag(request.userId, tag.id.get).map {
            case Some(userTag) =>
              success(Messages("success.found"), Json.obj(
                "id" -> tag.id.get,
                "tag" -> tag.name,
                "subscribed" -> true
              ))
            case None =>
              success(Messages("success.found"), Json.obj(
                "id" -> tag.id.get,
                "tag" -> tag.name,
                "subscribed" -> false
              ))
          }
        } else {
          Future.successful(success(Messages("success.found"), Json.obj()))
        }
      case None => Future.successful(success(Messages("success.found"), Json.obj()))
    }
  }

  def subscribeTag(tagId: Long) = SecuredAction.async { implicit request =>
    tagService.subscribeTag(request.userId, tagId).map(_ => success(Messages("success.subscribeTag")))
  }

  def unSubscribeTag(tagId: Long) = SecuredAction.async { implicit request =>
    tagService.unsubscribeTag(request.userId, tagId).map { rs =>
      if (rs == 1) {
        success(Messages("success.unSubscribeTag"))
      } else {
        error(4058, Messages("failed"))
      }
    }
  }

}
