package controllers

import javax.inject.Inject

import com.likeorz.event.{ LikeEventType, LikeEvent }
import com.likeorz.models.Entity
import com.likeorz.services.store.MongoDBService
import com.likeorz.utils.GlobalConstants
import play.api.cache.Cached
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import com.likeorz.services._
import utils.QiniuUtil

import scala.concurrent.Future
import scala.util.Random

class SearchController @Inject() (
    val messagesApi: MessagesApi,
    tagService: TagService,
    postService: PostService,
    userService: UserService,
    eventBusService: EventBusService,
    mongoDBService: MongoDBService,
    cached: Cached,
    promoteService: PromoteService) extends BaseController {

  def searchUsersAndTags(name: String) = Action.async {
    for {
      users <- userService.searchByName(name)
      tags <- tagService.autoComplete(name)
    } yield {
      val userJson = Json.toJson(users.map { user =>
        Json.obj(
          "user_id" -> user.identify,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes
        )
      })
      val tagJson = Json.toJson(tags.map(tag => Json.obj(
        "mark_id" -> tag.identify,
        "tag_id" -> tag.id.get,
        "tag" -> tag.name,
        "likes" -> 0
      )))
      success(Messages("success.found"), Json.obj(
        "tags" -> tagJson,
        "users" -> userJson
      ))
    }
  }

  def hotTags = Action.async {
    for {
      entities <- promoteService.getPromoteEntities(2)
      tags <- tagService.hotTags(15)
    } yield {
      val entityArr = (entities.toSet + Entity(None, "BJD", "", "") + Entity(None, "手绘", "", "")).map { entity => Json.obj("tag" -> entity.name) }
      val tagArr = tags.filterNot(t => entities.exists(_.name == t)).map { tag => Json.obj("tag" -> tag) }

      success(Messages("success.found"), Json.toJson(Random.shuffle(entityArr ++ tagArr)))
    }
  }

  @deprecated("replaced by v2", "v1.1.1")
  def searchTag(name: String, page: Int) = UserAwareAction.async { implicit request =>
    if (request.userId.isDefined && page == 0) {
      eventBusService.publish(LikeEvent(None, LikeEventType.search, "user", request.userId.get.toString, properties = Json.obj("query" -> name)))
      // Subscribe tag when user search
      tagService.getTagByName(name).map {
        case Some(tag) =>
          if (tag.group >= 0 && tag.usage >= 10) tagService.subscribeTag(request.userId.get, tag.id.get)
        case None =>
      }
    }
    for {
      entityOpt <- promoteService.getEntitybyName(name)
      results <- postService.searchByTag(page = page, name = name)
    } yield {
      val posts = results.map { post =>
        val user = userService.getUserInfoFromCache(post.userId)
        Json.obj(
          "post_id" -> post.id,
          "type" -> post.`type`.toString,
          "content" -> QiniuUtil.getPhoto(post.content, "medium"),
          "created" -> post.created,
          "user" -> Json.obj(
            "user_id" -> post.userId,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
            "likes" -> user.likes
          )
        )
      }
      if (entityOpt.isDefined) {
        success(Messages("success.found"), Json.obj(
          "info" -> Json.obj(
            "id" -> entityOpt.get.id.get,
            "name" -> entityOpt.get.name,
            "description" -> entityOpt.get.description,
            "avatar" -> QiniuUtil.getAvatar(entityOpt.get.avatar, "large")
          ),
          "posts" -> Json.toJson(posts)
        ))
      } else {
        success(Messages("success.found"), Json.obj(
          "posts" -> Json.toJson(posts)
        ))
      }
    }
  }

  def searchTagV2(tag: String, ts: Option[Long]) = UserAwareAction.async { implicit request =>
    val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
    if (request.userId.isDefined && ts.isEmpty) {
      eventBusService.publish(LikeEvent(None, LikeEventType.search, "user", request.userId.get.toString, properties = Json.obj("query" -> tag)))
      // Subscribe tag when user search
      tagService.getTagByName(tag).map {
        case Some(tag) =>
          if (tag.group >= 0 && tag.usage >= 10) tagService.subscribeTag(request.userId.get, tag.id.get)
        case None =>
      }
    }
    for {
      entityOpt <- if (ts.isEmpty) promoteService.getEntitybyName(tag) else Future.successful(None)
      posts <- postService.searchByTagAndTimestamp(tag, GlobalConstants.GridPageSize, ts)
    } yield {
      // Get marks for posts from mongodb
      val marksMap = mongoDBService.getPostMarksByIds(posts.map(_.id.get))

      val postsJson = posts.map { post =>
        val user = userService.getUserInfoFromCache(post.userId)
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
          "post_id" -> post.id,
          "type" -> post.`type`.toString,
          "thumbnail" -> QiniuUtil.getThumbnailImage(post.content),
          "preview" -> QiniuUtil.getSizedImage(post.content, screenWidth),
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
      if (entityOpt.isDefined) {
        success(Messages("success.found"), Json.obj(
          "info" -> Json.obj(
            "id" -> entityOpt.get.id.get,
            "name" -> entityOpt.get.name,
            "description" -> entityOpt.get.description,
            "avatar" -> QiniuUtil.getAvatar(entityOpt.get.avatar, "large")
          ),
          "posts" -> Json.toJson(postsJson),
          "next" -> posts.lastOption.map(_.created)
        ))
      } else {
        success(Messages("success.found"), Json.obj(
          "posts" -> Json.toJson(postsJson),
          "next" -> posts.lastOption.map(_.created)
        ))
      }
    }
  }

  def explore(tag: String, ts: Option[Long]) = cached(_ => "explore:" + tag + ":" + ts.getOrElse(""), duration = 300) {
    UserAwareAction.async { implicit request =>
      val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
      for {
        hotUsers <- if (ts.isEmpty) tagService.hotUsersForTag(tag, 15) else Future.successful(Seq())
        posts <- postService.searchByTagAndTimestamp(tag, GlobalConstants.GridPageSize, ts)
      } yield {
        val hotUsersJson = Json.toJson(hotUsers.map { user =>
          Json.obj(
            "user_id" -> user.id,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
            "likes" -> user.likes
          )
        })
        // Get marks for posts from mongodb
        val marksMap = mongoDBService.getPostMarksByIds(posts.map(_.id.get))

        val postsJson = posts.map { post =>
          val user = userService.getUserInfoFromCache(post.userId)
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
            "post_id" -> post.id,
            "type" -> post.`type`.toString,
            "content" -> QiniuUtil.getPhoto(post.content, "medium"),
            "thumbnail" -> QiniuUtil.getThumbnailImage(post.content),
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
        success(Messages("success.found"), Json.obj(
          "hot_users" -> hotUsersJson,
          "posts" -> Json.toJson(postsJson),
          "next" -> posts.lastOption.map(_.created)
        ))
      }
    }
  }

  def getEditorPicks(ts: Option[Long]) = UserAwareAction.async { implicit request =>
    val screenWidth = scala.math.min(960, (getScreenWidth * 1.5).toInt)
    postService.getEditorPickPostIds(GlobalConstants.GridPageSize, ts).flatMap { ids =>
      if (ids.isEmpty) {
        Future.successful(success(Messages("success.found"), Json.obj("posts" -> Json.arr())))
      } else {
        postService.getPostsByIdsSimple(ids).map(_.sortBy(-_.created)).map { posts =>
          if (posts.isEmpty) {
            success(Messages("success.found"), Json.obj("posts" -> Json.arr()))
          } else {
            // Get marks for posts from mongodb
            val marksMap = mongoDBService.getPostMarksByIds(posts.map(_.id.get))

            val postsJson = posts.map { post =>
              val marks = marksMap.getOrElse(post.id.get, Seq())
              val userInfo = userService.getUserInfoFromCache(post.userId)
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
                "content" -> QiniuUtil.getSizedImage(post.content, screenWidth),
                "thumbnail" -> QiniuUtil.getThumbnailImage(post.content),
                "preview" -> QiniuUtil.getSizedImage(post.content, screenWidth),
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
            success(Messages("success.found"), Json.obj("posts" -> Json.toJson(postsJson), "next" -> posts.last.created))
          }
        }
      }
    }
  }

  @deprecated("unsed", "v1.1.1")
  def hotUsers = UserAwareAction.async {
    for {
      hotUsers <- postService.get30DayHotUsers(3)
      newUsers <- postService.get30DayHotUsers(3)
    } yield {
      val hotJson = Json.toJson(hotUsers.map { user =>
        Json.obj(
          "user_id" -> user.id,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes
        )
      })
      val newJson = Json.toJson(newUsers.map { user =>
        Json.obj(
          "user_id" -> user.id,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes
        )
      })
      success(Messages("success.found"), Json.obj(
        "hot_users" -> hotJson,
        "new_users" -> newJson
      ))
    }
  }

  @deprecated("unsed", "v1.1.1")
  def hotTagsAndUsers = UserAwareAction.async {
    for {
      entities <- promoteService.getPromoteEntities(2)
      tags <- tagService.hotTags(13)
      hotUsers <- postService.get30DayHotUsers(15)
    } yield {
      val entityArr = entities.map { entity => Json.obj("tag" -> entity.name) }
      val tagArr = tags.filterNot(t => entities.exists(_.name == t)).map { tag => Json.obj("tag" -> tag) }

      val hotTagsJson = Json.toJson(Random.shuffle(entityArr ++ tagArr))

      val hotUsersJson = Json.toJson(hotUsers.map { user =>
        Json.obj(
          "user_id" -> user.id,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
          "likes" -> user.likes
        )
      })
      success(Messages("success.found"), Json.obj(
        "hot_tags" -> hotTagsJson,
        "hot_users" -> hotUsersJson
      ))
    }
  }

}
