package controllers

import javax.inject.Inject

import com.likeorz.services._
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.Admin
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.DashboardService

class TagApiController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[Admin, CookieAuthenticator],
    dashboardService: DashboardService,
    userService: UserService,
    postService: PostService,
    markService: MarkService,
    tagService: TagService) extends Silhouette[Admin, CookieAuthenticator] {

  def getGroupedTags = SecuredAction.async {
    tagService.getGroupedTags.map { result =>
      val groups = result.map {
        case (group, tags) =>
          val json = tags.map { tag =>
            Json.obj(
              "id" -> tag.id,
              "tag" -> tag.name,
              "group" -> tag.group,
              "usage" -> tag.usage
            )
          }
          Json.obj(
            "groupId" -> group.id,
            "groupName" -> group.name,
            "tags" -> Json.toJson(json)
          )
      }
      Ok(Json.toJson(groups))
    }
  }

  def getTagsForGroup(id: Long, page: Int, size: Int) = SecuredAction.async {
    tagService.getTagsForGroup(id, size, page).map { tags =>
      val json = tags.map { tag =>
        Json.obj(
          "id" -> tag.id,
          "tag" -> tag.name,
          "group" -> tag.group,
          "usage" -> tag.usage
        )
      }
      Ok(Json.toJson(json))
    }
  }

  def setTagGroup(id: Long, groupId: Long) = SecuredAction.async {
    tagService.setTagGroup(id, groupId).map(_ => Ok)
  }

  def addTagGroup(name: String) = SecuredAction.async {
    tagService.addTagGroup(name).map { tg =>
      Ok(Json.obj(
        "groupId" -> tg.id.get,
        "groupName" -> tg.name,
        "tags" -> Json.arr()
      ))
    }
  }
}
