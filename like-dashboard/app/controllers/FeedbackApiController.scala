package controllers

import javax.inject.Inject

import com.likeorz.services._
import com.likeorz.services.store.MongoDBService
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import models.Admin
import play.api.i18n.MessagesApi
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import utils.QiniuUtil

class FeedbackApiController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[Admin, CookieAuthenticator],
    userService: UserService,
    postService: PostService,
    infoService: InfoService,
    mongoDBService: MongoDBService) extends Silhouette[Admin, CookieAuthenticator] {

  def getFeedbackList(page: Int, pageSize: Int) = SecuredAction.async {
    for {
      feedbacks <- infoService.listFeedbacks(pageSize, page)
    } yield {
      val jsonList = Json.toJson(feedbacks.map { fb =>
        val user = userService.getUserInfoFromCache(fb.userId)
        Json.obj(
          "id" -> fb.id,
          "content" -> fb.feedback,
          "created" -> fb.created,
          "user" -> Json.obj(
            "userId" -> fb.userId,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.resizeImage(user.avatar, 50),
            "likes" -> user.likes
          )
        )
      })
      Ok(Json.obj(
        "feedbacks" -> jsonList
      ))
    }
  }

  def deleteFeedback(id: Long) = SecuredAction.async {
    infoService.deleteFeedback(id).map(_ => Ok)
  }

  def getReportList(page: Int, pageSize: Int) = SecuredAction.async {
    for {
      reports <- infoService.listReports(pageSize, page)
      postMap <- postService.getPostsByIdsSimple(reports.map(_.postId)).map(list => list.map(x => x.id.get -> x).toMap)
    } yield {
      val marksMap = mongoDBService.getPostMarksByIds(reports.map(_.postId))
      val jsonList = Json.toJson(reports.filter(r => postMap.contains(r.postId)).map { rp =>
        val user = userService.getUserInfoFromCache(rp.userId)
        val post = postMap(rp.postId)
        val postAuthor = userService.getUserInfoFromCache(post.userId)
        val marks = marksMap.getOrElse(post.id.get, Seq())
        val marksJson = marks.map { mark =>
          Json.obj(
            "markId" -> mark.markId,
            "tag" -> mark.tag,
            "likes" -> mark.likes.size,
            "likedBy" -> Json.toJson(mark.likes)
          )
        }
        Json.obj(
          "id" -> rp.id,
          "reason" -> rp.reason,
          "created" -> rp.created,
          "user" -> Json.obj(
            "userId" -> rp.userId,
            "nickname" -> user.nickname,
            "avatar" -> QiniuUtil.resizeImage(user.avatar, 50),
            "likes" -> user.likes
          ),
          "post" -> Json.obj(
            "postId" -> post.id.get,
            "thumbnail" -> QiniuUtil.resizeImage(post.content, 200, isParsedFilename = true),
            "preview" -> QiniuUtil.resizeImage(post.content, 600, isParsedFilename = true),
            "hidden" -> (post.score.get < 0),
            "user" -> Json.obj(
              "user_id" -> post.userId,
              "nickname" -> postAuthor.nickname,
              "avatar" -> QiniuUtil.squareImage(postAuthor.avatar, 100),
              "likes" -> postAuthor.likes
            ),
            "marks" -> Json.toJson(marksJson)
          )
        )
      })
      Ok(Json.obj(
        "reports" -> jsonList
      ))
    }
  }

  def deleteReport(id: Long) = SecuredAction.async {
    infoService.deleteReport(id).map(_ => Ok)
  }

}
