package controllers

import javax.inject.Inject

import com.likeorz.services.store.MongoDBService
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
    userSettingService: UserSettingService,
    postService: PostService,
    markService: MarkService,
    mongoDBService: MongoDBService,
    promoteService: PromoteService) extends Silhouette[Admin, CookieAuthenticator] {

  def fetchUserList(page: Int, pageSize: Int, filter: String) = SecuredAction.async {
    for {
      users <- userService.filterUsersByNameAndMobile(pageSize, page, filter)
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
            "mobile" -> user.mobile,
            "email" -> user.email,
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
    val screenWidth = 600
    val pageSize = 48
    userService.findById(id).flatMap {
      case Some(user) =>
        postService.getRecentPostsForUser(id, pageSize, timestamp).flatMap { ids =>
          if (ids.isEmpty) {
            Future.successful(Ok(Json.obj("posts" -> Json.arr())))
          } else {
            postService.getPostsByIdsSimple(ids).flatMap { posts =>
              if (posts.isEmpty) {
                Future.successful(Ok(Json.obj("posts" -> Json.arr())))
              } else {

                val marksMap = mongoDBService.getPostMarksByIds(ids)

                val sortedPosts = posts.sortBy(-_.created)

                val futures = sortedPosts.map { post =>

                  for {
                    isRecommended <- dashboardService.isPostRecommended(post.id.get)
                    isBlocked <- dashboardService.isPostBlocked(post.id.get)
                  } yield {
                    val marks = marksMap.getOrElse(post.id.get, Seq())
                    val userInfo = userService.getUserInfoFromCache(post.userId)
                    val marksJson = marks.map { mark =>
                      Json.obj(
                        "markId" -> mark.markId,
                        "tag" -> mark.tag,
                        "likes" -> mark.likes.size,
                        "likedBy" -> Json.toJson(mark.likes)
                      )
                    }
                    Json.obj(
                      "id" -> post.id.get,
                      "type" -> post.`type`,
                      "content" -> QiniuUtil.squareImage(post.content, screenWidth),
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
                  Ok(Json.obj("posts" -> Json.toJson(posts), "next" -> sortedPosts.last.created))
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

  def unregisterUser(id: Long) = SecuredAction.async {
    userService.findById(id).flatMap {
      case Some(user) =>
        println(user)
        userSettingService.unregister(id).map { rs =>
          println(rs)
          if (rs == 1) Ok
          else BadRequest
        }
      case None => Future.successful(NotFound)
    }
  }

}
