package controllers

import javax.inject.{ Named, Inject }

import akka.actor.ActorRef
import com.likeorz.event.LikeEvent
import play.api.cache.Cached
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import services.{ PromoteService, PostService, TagService }
import utils.QiniuUtil

import scala.util.Random

class SearchController @Inject() (
    @Named("event-producer-actor") eventProducerActor: ActorRef,
    val messagesApi: MessagesApi,
    tagService: TagService,
    postService: PostService,
    cached: Cached,
    promoteService: PromoteService) extends BaseController {

  def autoComplete(name: String) = Action.async {
    tagService.autoComplete(name).map { tags =>
      success(Messages("success.found"), Json.toJson(tags.map(tag => Json.obj(
        "id" -> tag.id.toString,
        "tag" -> tag.tagName,
        "likes" -> tag.likes
      )))
      )
    }
  }

  def hotTags = Action.async {
    for {
      entities <- promoteService.getPromoteEntities(2)
      tags <- tagService.hotTags(15)
    } yield {
      val entityArr = entities.map { entity =>
        val image = entity.images.map { images =>
          val list = images.split(",").filter(_.trim.length > 0)
          list(Random.nextInt(list.length))
        }.getOrElse(entity.avatar)
        Json.obj(
          "tag" -> entity.name,
          "image" -> QiniuUtil.getScale(image, 360)
        )
      }
      val tagArr = tags.filterNot(t => entities.exists(_.name == t)).map { tag => Json.obj("tag" -> tag) }

      success(Messages("success.found"), Json.toJson(Random.shuffle(entityArr ++ tagArr)))
    }
  }

  def searchTag(name: String, page: Int) = UserAwareAction.async { implicit request =>
    if (request.userId.isDefined && page == 0)
      eventProducerActor ! LikeEvent(None, "search", "user", request.userId.get.toString, properties = Json.obj("query" -> name))
    postService.searchByTag(page = page, name = name).map { results =>
      val posts = results.map {
        case (post, user) => Json.obj(
          "post_id" -> post.id,
          "type" -> post.`type`.toString,
          "content" -> QiniuUtil.getPhoto(post.content, "medium"),
          "created" -> post.created,
          "user" -> Json.obj(
            "user_id" -> user.id,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
            "likes" -> user.likes
          )
        )
      }
      success(Messages("success.found"), Json.toJson(posts))
    }
  }

  def searchTagV2(name: String, page: Int) = UserAwareAction.async { implicit request =>
    if (request.userId.isDefined && page == 0)
      eventProducerActor ! LikeEvent(None, "search", "user", request.userId.get.toString, properties = Json.obj("query" -> name))
    for {
      entityOpt <- promoteService.getEntitybyName(name)
      results <- postService.searchByTag(page = page, name = name)
    } yield {
      val posts = results.map {
        case (post, user) => Json.obj(
          "post_id" -> post.id,
          "type" -> post.`type`.toString,
          "content" -> QiniuUtil.getPhoto(post.content, "medium"),
          "created" -> post.created,
          "user" -> Json.obj(
            "user_id" -> user.id,
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

  def explore(tag: String) = cached(_ => "explore:" + tag, duration = 600) {
    UserAwareAction.async { implicit request =>
      for {
        hotUsers <- tagService.hotUsersForTag(tag, 15)
        results <- postService.searchByTag(0, 30, tag)
      } yield {
        val hotUsersJson = Json.toJson(hotUsers.map { user =>
          Json.obj(
            "user_id" -> user.id,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
            "likes" -> user.likes
          )
        })
        val posts = results.map {
          case (post, user) => Json.obj(
            "post_id" -> post.id,
            "type" -> post.`type`.toString,
            "content" -> QiniuUtil.getPhoto(post.content, "medium"),
            "created" -> post.created,
            "user" -> Json.obj(
              "user_id" -> user.id,
              "nickname" -> user.nickname,
              "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
              "likes" -> user.likes
            )
          )
        }
        success(Messages("success.found"), Json.obj(
          "hot_users" -> hotUsersJson,
          "posts" -> Json.toJson(posts)
        ))
      }
    }
  }

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
