package controllers

import javax.inject.Inject

import com.likeorz.models.{ UserTag, Notification }
import com.likeorz.push.JPushNotification
import com.likeorz.services.store.MongoDBService
import com.likeorz.utils.GlobalConstants
import play.api.Play
import play.api.cache.Cached
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play.current
import play.api.mvc.Action
import com.likeorz.services._
import services.PushService
import utils.QiniuUtil

import scala.concurrent.Future
import scala.util.Random

class UserController @Inject() (
    val messagesApi: MessagesApi,
    cached: Cached,
    userService: UserService,
    tagService: TagService,
    markService: MarkService,
    postService: PostService,
    mongoDBService: MongoDBService,
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
          countFavorite <- if (request.userId.isDefined && request.userId.get == id) postService.countFavoriteForUser(id) else Future.successful(0L)
          countLikes <- markService.countLikesForUser(id)
          isFollowing <- if (request.userId.isDefined) userService.isFollowing(request.userId.get, id) else Future.successful(0)
        } yield {
          success(Messages("success.found"), Json.obj(
            "user_id" -> id.toString,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.getAvatar(user.avatar, "large"),
            "origin_avatar" -> QiniuUtil.getRaw(user.avatar),
            "cover" -> QiniuUtil.getSizedImage(user.cover, getScreenWidth),
            "likes" -> countLikes,
            "is_following" -> isFollowing,
            // Todo change after v1.1.1
            "count" -> Json.obj(
              "post" -> countPosts,
              "follow" -> countFollowing,
              "fan" -> countFollowers,
              "posts" -> countPosts,
              "following" -> countFollowing,
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

  @deprecated("Use timestamp anchor", "v1.1.1")
  def getPostsForUserV1(id: Long, page: Int) = UserAwareAction.async { implicit request =>
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

  def getPostsForUserV2(id: Long, ts: Option[Long]) = UserAwareAction.async { implicit request =>
    val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
    userService.findById(id).flatMap {
      case Some(user) =>
        postService.getPostsForUser(id, GlobalConstants.GridPageSize, ts).map { posts =>
          if (posts.isEmpty) {
            success(Messages("success.found"), Json.obj("posts" -> Json.arr()))
          } else {
            // Get marks for posts from mongodb
            val marksMap = mongoDBService.getPostMarksByIds(posts.map(_.id.get))

            val postsJson = posts.map { post =>
              val marks = marksMap.getOrElse(post.id.get, Seq())
              val marksJson = marks.map { mark =>
                Json.obj(
                  "mark_id" -> mark.markId,
                  "tag" -> mark.tag,
                  "likes" -> mark.likes.size,
                  "is_liked" -> {
                    if (request.userId.isDefined) mark.likes.contains(request.userId.get) else false
                  }
                )
              }
              Json.obj(
                "post_id" -> post.id.get,
                "type" -> post.`type`,
                "content" -> QiniuUtil.getPhoto(post.content, "medium"),
                "thumbnail" -> QiniuUtil.getPhoto(post.content, "medium"),
                "preview" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                "raw_image" -> QiniuUtil.getRaw(post.content),
                "created" -> post.created,
                "user" -> Json.obj(
                  "user_id" -> post.userId,
                  "nickname" -> user.nickname,
                  "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
                  "likes" -> user.likes
                ),
                "marks" -> Json.toJson(marksJson)
              )
            }
            success(Messages("success.found"), Json.obj("posts" -> Json.toJson(postsJson), "next" -> posts.last.created))
          }
        }
      case None => Future.successful(error(4022, Messages("invalid.userId")))
    }
  }

  def getFavoritePostsForUser(ts: Option[Long]) = SecuredAction.async { implicit request =>
    val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
    postService.getFavoritePostsForUser(request.userId, GlobalConstants.GridPageSize, ts).flatMap { results =>
      postService.getPostsByIdsSimple(results.keySet.toSeq).map { posts =>
        if (posts.isEmpty) {
          success(Messages("success.found"), Json.obj("posts" -> Json.arr()))
        } else {
          // Get marks for posts from mongodb
          val marksMap = mongoDBService.getPostMarksByIds(posts.map(_.id.get))

          val postsJson = posts.sortBy(p => -results(p.id.get)).map { post =>
            val marks = marksMap.getOrElse(post.id.get, Seq())
            val userInfo = userService.getUserInfoFromCache(post.userId)
            val marksJson = marks.map { mark =>
              Json.obj(
                "mark_id" -> mark.markId,
                "tag" -> mark.tag,
                "likes" -> mark.likes.size,
                "is_liked" -> mark.likes.contains(request.userId)
              )
            }
            Json.obj(
              "post_id" -> post.id.get,
              "type" -> post.`type`,
              "content" -> QiniuUtil.getPhoto(post.content, "medium"),
              "thumbnail" -> QiniuUtil.getPhoto(post.content, "medium"),
              "preview" -> QiniuUtil.getSizedImage(post.content, screenWidth),
              "raw_image" -> QiniuUtil.getRaw(post.content),
              "created" -> post.created,
              "favorited" -> true,
              "user" -> Json.obj(
                "user_id" -> post.userId,
                "nickname" -> userInfo.nickname,
                "avatar" -> QiniuUtil.getAvatar(userInfo.avatar, "small"),
                "likes" -> userInfo.likes
              ),
              "marks" -> Json.toJson(marksJson)
            )
          }
          success(Messages("success.found"), Json.obj("posts" -> Json.toJson(postsJson), "next" -> results.values.min))
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

  def getFollowing(id: Long, page: Int) = Action.async {
    userService.getFollowings(id, page).map { list =>
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
    userService.getFollowers(id, page).map { list =>
      val jsonArr = list.map { user =>
        Json.obj(
          "user_id" -> user.id.toString,
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

  def getUserTag(tagName: String) = UserAwareAction.async { implicit request =>

    tagService.getTagByName(tagName).flatMap {
      case Some(tag) =>
        if (tag.group > 0) {
          tagService.getUserTag(request.userId.getOrElse(-1L), tag.id.get).map {
            case Some(userTag) =>
              success(Messages("success.found"), Json.obj(
                "tag" -> Json.obj(
                  "id" -> tag.id.get,
                  "tag" -> tag.name,
                  "subscribed" -> userTag.subscribe
                )
              ))
            case None =>
              success(Messages("success.found"), Json.obj(
                "tag" -> Json.obj(
                  "id" -> tag.id.get,
                  "tag" -> tag.name,
                  "subscribed" -> false
                )
              ))
          }
        } else {
          Future.successful(success(Messages("success.found"), Json.obj()))
        }
      case None => Future.successful(success(Messages("success.found"), Json.obj()))
    }
  }

  def subscribeTag(tagId: Long) = SecuredAction.async { implicit request =>
    tagService.getTagById(tagId).flatMap {
      case Some(tag) =>
        if (tag.group > 0) {
          tagService.subscribeTag(request.userId, tagId).map(_ => success(Messages("success.subscribeTag")))
        } else {
          Future.successful(error(4058, Messages("failed.subscribeTag")))
        }
      case None => Future.successful(error(4058, Messages("failed.subscribeTag")))
    }

  }

  def unSubscribeTag(tagId: Long) = SecuredAction.async { implicit request =>
    for {
      tag <- tagService.getTagById(tagId)
      rs <- tagService.unsubscribeTag(request.userId, tagId)
    } yield {
      if (rs == 1 && tag.isDefined) {
        mongoDBService.removeTimelineFeedForUserWhenUnsubscribeTag(request.userId, tag.get.name)
        success(Messages("success.unSubscribeTag"))
      } else {
        error(4058, Messages("failed"))
      }
    }
  }

  def getRecommendTags = cached("welcomeTags") {
    Action.async {
      val tags = Seq("电影", "玩具", "美食", "吃货", "手办", "粘土", "外设", "耳机", "车模", "手绘", "动漫", "摄影", "旅行", "机车", "创意", "海贼王", "高达", "钢铁侠", "萌", "猫", "DIY", "设计", "圣斗士", "乐高", "变形金刚")

      val futures = tags.map { tagName =>
        tagService.getTagWithImage(tagName)
      }

      Future.sequence(futures).map { x =>
        val jsonArr = Random.shuffle(x.flatten).map {
          case (tag, imgOpt) => Json.obj(
            "id" -> tag.id.get,
            "tag" -> tag.name,
            "likes" -> tag.usage,
            "group" -> tag.group,
            "image" -> imgOpt.map(QiniuUtil.getPhoto(_, "medium"))
          )
        }
        success(Messages("success.found"), Json.obj(
          "tags" -> Json.toJson(jsonArr)
        ))
      }
    }
  }

  def choosePreferTags = SecuredAction.async(parse.json) { implicit request =>
    val groupIds = (request.body \ "groups").as[Seq[Long]]
    val tagIds = (request.body \ "tags").as[Seq[Long]]

    tagService.getGroupedTags.flatMap { results =>
      val tags = results.filter(g => groupIds.contains(g._1.id.get))
        .flatMap { pair =>
          val topN = pair._2.length / 3
          pair._2.sortBy(_.usage).reverse.take(topN)
        }
      val userTags = tags.map(tag => UserTag(request.userId, tag.id.get)).toSeq

      for {
        subscribedIds <- tagService.getUserSubscribeTagIds(request.userId)
        result <- tagService.subscribeTags(userTags.filterNot(t => subscribedIds.contains(t.tagId)))
      } yield {
        success(Messages("success.found"))
      }
    }
  }

}
