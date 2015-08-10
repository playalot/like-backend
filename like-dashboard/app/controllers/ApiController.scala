package controllers

import javax.inject.Inject

import com.likeorz.models.Post
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.Admin
import play.api.Configuration
import play.api.i18n.MessagesApi
import com.likeorz.services._
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.{ AdminService, DashboardService }
import utils.QiniuUtil

import scala.concurrent.Future

class ApiController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[Admin, CookieAuthenticator],
    adminService: AdminService,
    dashboardService: DashboardService,
    userService: UserService,
    postService: PostService,
    markService: MarkService,
    notificationService: NotificationService,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    configuration: Configuration,
    clock: Clock) extends Silhouette[Admin, CookieAuthenticator] {

  def stats = SecuredAction.async { implicit request =>
    adminService.stats.map { stats =>
      Ok(Json.toJson(stats))
    }
  }

  /**
   * Display the paginated list of computers.
   *
   * @param page Current page number (starts from 0)
   */
  def list(page: Int) = SecuredAction.async { implicit request =>
    val posts = dashboardService.list(page = page)
    implicit val postFormat = Json.format[Post]
    posts.map { page =>
      val items = page.items.map { item =>
        val marks = item._2.map { mark =>
          Json.obj(
            "markId" -> mark._1,
            "tag" -> mark._2,
            "likes" -> mark._3
          )
        }
        Json.obj(
          "userId" -> item._1.userId,
          "postId" -> item._1.id,
          "postUrl" -> QiniuUtil.resizeImage(item._1.content, 300),
          "marks" -> Json.toJson(marks)
        )
      }
      Ok(Json.obj(
        "posts" -> Json.toJson(items),
        "page" -> Json.obj(
          "currentPage" -> page.page,
          "total" -> page.total,
          "prev" -> page.prev,
          "next" -> page.next,
          "pageSize" -> 36
        )))
    }
  }

  def userInfo(userId: Long) = SecuredAction.async { implicit request =>
    userService.getUserInfo(userId).map { u =>
      Ok(Json.obj(
        "nickname" -> u.nickname,
        "avatar" -> QiniuUtil.resizeImage(u.avatar, 40),
        "likes" -> u.likes
      ))
    }
  }

  def deleteMark(markId: Long) = SecuredAction.async {
    markService.deleteMark(markId).map { _ =>
      Ok("success.deleteMark")
    }
  }

  def deletePost(postId: Long) = SecuredAction.async {
    postService.getPostById(postId).flatMap {
      case Some(post) =>
        for {
          p <- postService.deletePostById(postId, post.userId)
          n <- notificationService.deleteAllNotificationForPost(postId)
          r <- postService.recordDelete(post.content)
        } yield {
          Ok("success.deletePost")
        }
      case None => Future.successful(NotFound)
    }
  }

  def recommendPost(postId: Long, status: Boolean) = SecuredAction.async {
    dashboardService.recommendPost(postId, status).map(_ => Ok)
  }

  def invisiblePost(postId: Long, status: Boolean) = SecuredAction.async {
    dashboardService.blockPost(postId, status).map(_ => Ok)
  }

  def isPostRecommended(postId: Long) = SecuredAction.async {
    dashboardService.isPostRecommended(postId).map(x => Ok(Json.obj("status" -> x)))
  }

  def isPostInvisible(postId: Long) = SecuredAction.async {
    dashboardService.isPostBlocked(postId).map(x => Ok(Json.obj("status" -> x)))
  }

}
