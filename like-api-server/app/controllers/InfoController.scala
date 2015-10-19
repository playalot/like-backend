package controllers

import javax.inject.Inject

import com.likeorz.models._
import com.likeorz.services.{ UserService, InfoService }
import com.likeorz.utils.{ KeyUtils, RedisCacheClient, AVOSUtils }
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json

import scala.concurrent.Future

class InfoController @Inject() (
    val messagesApi: MessagesApi,
    infoService: InfoService,
    userService: UserService) extends BaseController {

  /** User feedback */
  def feedback = SecuredAction.async(parse.json) { implicit request =>
    (request.body \ "feedback").asOpt[String] match {
      case Some(fb) => infoService.addFeedback(Feedback(None, request.userId, fb)).map { _ =>
        success(Messages("success.feedback"))
      }
      case None => Future.successful(error(4027, Messages("invalid.feedback")))
    }
  }

  /** Report abuse of a post */
  def report(postId: Long) = (SecuredAction andThen BannedUserCheckAction).async { implicit request =>
    val reason = request.body.asJson match {
      case Some(json) => (json \ "reason").asOpt[String]
      case None       => None
    }
    infoService.addReport(Report(None, request.userId, postId, reason = reason)).map { _ =>
      success("success.report")
    }
  }

  def installation = SecuredAction.async(parse.json) { implicit request =>
    val tokenOpt = (request.body \ "device_token").asOpt[String]
    val typeOpt = (request.body \ "device_type").asOpt[String]

    (tokenOpt, typeOpt) match {
      case (Some(token), Some(t)) => infoService.findInstallation(t, request.userId).flatMap {
        case Some(installation) =>
          val tsDiff = ((System.currentTimeMillis() / 1000) - installation.updated) / 3600
          if (installation.deviceToken != token || tsDiff > 24) {
            AVOSUtils.updateInstallations(installation.objectId, token, t).flatMap { objectId =>
              infoService.updateInstallation(installation.id.get, installation.copy(objectId = objectId, userId = request.userId, status = 1, updated = System.currentTimeMillis() / 1000))
            }.map(_ => success(Messages("success.install")))
          } else {
            Future.successful(success(Messages("success.install")))
          }
        case None =>
          AVOSUtils.installations(token, t).flatMap { objectId =>
            infoService.insertInstallation(Installation(None, request.userId, objectId, token, t, 1))
              .map(_ => success(Messages("success.install")))
          }
      }
      case _ => Future.successful(error(4028, Messages("invalid.installation")))
    }
  }

  def getLikeRankToday = UserAwareAction.async {
    val rankYesterdayMap = RedisCacheClient.zrevrangeWithScores(KeyUtils.likeRankYesterday, 0, 50).zipWithIndex.map { x =>
      val userId = x._1._1.toLong
      val rankYesterday = x._2 + 1
      (userId, rankYesterday)
    }.toMap

    val jsonArr = RedisCacheClient.zrevrangeWithScores(KeyUtils.likeRankToday, 0, 50).zipWithIndex.map { x =>
      val userId = x._1._1.toLong
      val likes = x._1._2.toLong
      val rankToday = x._2 + 1
      val rankYesterday = rankYesterdayMap.getOrElse(userId, 100)
      val up = if (rankYesterday > rankToday) 1 else if (rankYesterday == rankToday) 0 else -1
      Json.obj(
        "user_id" -> userId,
        "likes" -> likes,
        "rank" -> rankToday,
        "up" -> up
      )
    }

    Future.successful(success(Messages("success.found"), Json.obj("ranks" -> Json.toJson(jsonArr))))
  }

}
