package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import services.{ PromoteService, PostService, TagService }
import utils.QiniuUtil

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class SearchController @Inject() (
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

  //  def hotTags = Action.async {
  //    tagService.hotTags.map { tags =>
  //      Ok(Json.obj(
  //        "code" -> 1,
  //        "message" -> "Record(s) Found",
  //        "data" -> Json.toJson(tags.map(tag => Json.obj(
  //          "id" -> tag.id,
  //          "tag" -> tag.tagName,
  //          "likes" -> tag.likes
  //        )))))
  //    }
  //  }
  def hotTags = Action.async {
    //    promoteService.getPromoteEntities().flatMap { entities =>
    //      entities.map(entity => postService.getTagPostImage(entity.name)
    //    }
    // Random select image for tag

    for {
      entities <- promoteService.getPromoteEntities()
      tags <- tagService.hotTags
    } yield {
      val entityArr = entities.map { entity =>
        Json.obj(
          "id" -> entity.id,
          "tag" -> entity.name,
          "likes" -> 0,
          "image" -> QiniuUtil.getAvatar(entity.avatar, "medium")
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

  def searchTag(page: Int, name: String) = Action.async { implicit request =>
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

  def searchTagV2(page: Int, name: String) = Action.async { implicit request =>
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
            "avatar" -> QiniuUtil.getAvatar(entityOpt.get.avatar, "medium")
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
