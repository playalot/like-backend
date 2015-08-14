package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.Admin
import com.likeorz.services._
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.DashboardService
import utils.QiniuUtil

import scala.concurrent.Future

class UserApiController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[Admin, CookieAuthenticator],
    dashboardService: DashboardService,
    userService: UserService,
    postService: PostService,
    markService: MarkService,
    promoteService: PromoteService) extends Silhouette[Admin, CookieAuthenticator] {

  def fetchUserList(page: Int, pageSize: Int, filter: String) = SecuredAction.async {

    for {
      users <- userService.listUsers(pageSize, page, filter)
    } yield {
      val jsonList = Json.toJson(users.map { user =>
        Json.obj(
          "id" -> user.id,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.resizeImage(user.avatar, 50),
          "mobile" -> user.mobile
        )
      })
      Ok(Json.obj(
        "users" -> jsonList
      ))
    }
  }

  def showUser(id: Long) = SecuredAction.async {
    Future.successful(Ok)
  }

}
