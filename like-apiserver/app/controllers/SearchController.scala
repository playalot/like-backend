package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import services.{ PostService, TagService }
import utils.QiniuUtil

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class SearchController @Inject() (
    val messagesApi: MessagesApi,
    tagService: TagService,
    postService: PostService) extends BaseController {

  def autoComplete(name: String) = Action.async {
    tagService.autoComplete(name).map { tags =>
      Ok(Json.obj(
        "code" -> 1,
        "message" -> "Record(s) Found",
        "data" -> Json.toJson(tags.map(tag => Json.obj(
          "id" -> tag.id.toString,
          "tag" -> tag.tagName,
          "likes" -> tag.likes
        )))
      ))
    }
  }

  def hotTags = Action.async {
    tagService.hotTags.map { tags =>
      Ok(Json.obj(
        "code" -> 1,
        "message" -> "Record(s) Found",
        "data" -> Json.toJson(tags.map(tag => Json.obj(
          "id" -> tag.id,
          "tag" -> tag.tagName,
          "likes" -> tag.likes
        )))))
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
      Ok(Json.obj(
        "code" -> 1,
        "message" -> "Record(s) Found",
        "data" -> Json.toJson(posts)
      ))
    }
  }

}
