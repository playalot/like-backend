package controllers

import javax.inject.Inject

import com.likeorz.models.User
import com.likeorz.services.{ PromoteService, MarkService, PostService, UserService }
import com.likeorz.utils.{ GlobalConstants, HashUtils, GenerateUtils }
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.Admin
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.{ AdminService, DashboardService }
import utils.QiniuUtil

import scala.concurrent.Future
import scala.util.Random

class AdminApiController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[Admin, CookieAuthenticator],
    adminService: AdminService,
    dashboardService: DashboardService,
    userService: UserService,
    postService: PostService,
    markService: MarkService,
    promoteService: PromoteService) extends Silhouette[Admin, CookieAuthenticator] {

  def createFakeUser = SecuredAction.async {
    val mobilePhoneNumber = 10000000 - Random.nextInt(10000)
    val fakeUser = User(None, Some("666" + mobilePhoneNumber), None,
      HashUtils.hashPassword("666666"),
      GlobalConstants.DefaultNickname,
      GlobalConstants.DefaultAvatar, GlobalConstants.DefaultCover,
      refreshToken = Some(HashUtils.hashPassword("666666"))
    )
    userService.insert(fakeUser).map { user =>
      Ok(Json.obj(
        "user_id" -> user.id.get,
        "avatar" -> QiniuUtil.resizeImage(user.avatar, 50),
        "cover" -> user.cover,
        "nickname" -> user.nickname
      ))
    }
  }

  def listFakeUsers = SecuredAction.async {
    adminService.listFakeUsers.map { users =>
      val usersJson = users.sortBy(_.id.get).map { user =>
        Json.obj(
          "user_id" -> user.id.get,
          "avatar" -> QiniuUtil.resizeImage(user.avatar, 50),
          "cover" -> user.cover,
          "nickname" -> user.nickname,
          "mobile" -> user.mobile
        )
      }
      Ok(Json.toJson(usersJson))
    }
  }

}
