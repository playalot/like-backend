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
    infoService: InfoService) extends Silhouette[Admin, CookieAuthenticator] {

  def fetchFeedbackList(page: Int, pageSize: Int) = SecuredAction.async {
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

}
