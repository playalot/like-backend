package controllers

import javax.inject.Inject

import com.likeorz.models.Post
import com.likeorz.utils.{ KeyUtils, RedisCacheClient }
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.Admin
import play.api.Configuration
import play.api.i18n.{ Messages, MessagesApi }
import com.likeorz.services._
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.{ AdminService, DashboardService }
import utils.QiniuUtil

import scala.concurrent.Future

class PostApiController @Inject() (
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

  def fetchPostList(timestamp: Option[Long]) = SecuredAction.async { implicit request =>
    // Use phone screen width for output photo size
    val screenWidth = 300
    val pageSize = 48

    postService.getRecentPosts(pageSize, timestamp).flatMap { ids =>
      if (ids.isEmpty) {
        Future.successful(Ok(Json.obj("posts" -> Json.arr())))
      } else {
        postService.getPostsByIds(ids).flatMap { list =>
          if (list.isEmpty) {
            Future.successful(Ok(Json.obj("posts" -> Json.arr())))
          } else {
            val sortedList = list.sortBy(-_._1.created)
            val futures = sortedList.map { row =>
              val post = row._1
              val markIds = row._2.map(_._1)

              for {
                userInfo <- userService.getUserInfo(post.userId)
                isRecommended <- dashboardService.isPostRecommended(post.id.get)
                isBlocked <- dashboardService.isPostBlocked(post.id.get)
              } yield {
                val marksJson = row._2.sortBy(-_._3).map { fields =>
                  Json.obj(
                    "markId" -> fields._1,
                    "tag" -> fields._2,
                    "likes" -> fields._3
                  )
                }
                Json.obj(
                  "id" -> post.id.get,
                  "type" -> post.`type`,
                  "content" -> QiniuUtil.resizeImage(post.content, screenWidth),
                  "created" -> post.created,
                  "isRecommended" -> isRecommended,
                  "isBlocked" -> isBlocked,
                  "user" -> Json.obj(
                    "userId" -> post.userId,
                    "nickname" -> userInfo.nickname,
                    "avatar" -> QiniuUtil.resizeImage(userInfo.avatar, 50),
                    "likes" -> userInfo.likes
                  ),
                  "marks" -> Json.toJson(marksJson)
                )
              }
            }
            Future.sequence(futures).map { posts =>
              Ok(Json.obj("posts" -> Json.toJson(posts), "nextTimestamp" -> sortedList.last._1.created))
            }
          }
        }
      }
    }
  }

}
