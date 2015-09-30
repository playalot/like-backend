package controllers

import javax.inject.Inject

import com.likeorz.services._
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
    infoService: InfoService) extends Silhouette[Admin, CookieAuthenticator] {

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
      val jsonList = Json.toJson(reports.filter(r => postMap.contains(r.postId)).map { rp =>
        val user = userService.getUserInfoFromCache(rp.userId)
        val post = postMap(rp.postId)
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
            "image" -> QiniuUtil.resizeImage(post.content, 200, true),
            "hidden" -> (post.score.get < 0)
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
