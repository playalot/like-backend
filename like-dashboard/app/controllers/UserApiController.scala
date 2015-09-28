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
          "mobile" -> user.mobile,
          "likes" -> user.likes
        )
      })
      Ok(Json.obj(
        "users" -> jsonList
      ))
    }
  }

  def getUserInfo(id: Long) = SecuredAction.async {
    userService.findById(id).flatMap {
      case Some(user) =>
        for {
          countFollowers <- userService.countFollowers(id)
          countFriends <- userService.countFollowing(id)
          countPosts <- postService.countPostsForUser(id)
          countLikes <- markService.countLikesForUser(id)
        } yield {
          Ok(Json.obj(
            "userId" -> id,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.resizeImage(user.avatar, 150),
            "cover" -> QiniuUtil.resizeImage(user.cover, 300),
            "likes" -> countLikes,
            "count" -> Json.obj(
              "posts" -> countPosts,
              "following" -> countFriends,
              "followers" -> countFollowers
            )
          ))
        }
      case None => Future.successful(NotFound)
    }
  }

  def getUserPosts(id: Long, timestamp: Option[Long]) = SecuredAction.async {
    val screenWidth = 300
    val pageSize = 48
    userService.findById(id).flatMap {
      case Some(user) =>
        postService.getRecentPostsForUser(id, pageSize, timestamp).flatMap { ids =>
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
      case None =>
        Future.successful(NotFound)
    }
  }

  def getActiveUsers(duration: Long) = SecuredAction {
    val userJson = userService.getActiveUserIds(duration).sortBy(-_._2).map {
      case (userId, timestamp) =>
        val user = userService.getUserInfoFromCache(userId)
        Json.obj(
          "id" -> userId,
          "nickname" -> user.nickname,
          "avatar" -> QiniuUtil.resizeImage(user.avatar, 50),
          "likes" -> user.likes,
          "lastSeen" -> timestamp
        )
    }
    Ok(Json.obj(
      "users" -> userJson,
      "total" -> userJson.length
    ))
  }

}
