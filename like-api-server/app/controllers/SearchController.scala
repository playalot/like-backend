package controllers

import javax.inject.{ Named, Inject }

import akka.actor.ActorRef
import com.likeorz.event.LikeEvent
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
      entities <- promoteService.getPromoteEntities()
      tags <- tagService.hotTags
    } yield {
      val entityArr = Random.shuffle(entities).take(1).map { entity =>
        val image = entity.images.map { images =>
          val list = images.split(",").filter(_.trim.length > 0)
          list(Random.nextInt(list.size))
        }.getOrElse(entity.avatar)
        Json.obj(
          "id" -> entity.id,
          "tag" -> entity.name,
          "likes" -> 0,
          "image" -> QiniuUtil.getScale(image, 360)
        )
      }
      val tagArr = tags.filterNot(t => entities.exists(_.name == t.tagName)).map { tag =>
        Json.obj(
          "id" -> tag.id,
          "tag" -> tag.tagName,
          "likes" -> tag.likes
        )
      }
      success(Messages("success.found"), Json.toJson(entityArr ++ tagArr))
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

}
