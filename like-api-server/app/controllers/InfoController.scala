package controllers

import javax.inject.Inject

import models.{ Installation, Feedback }
import services.InfoService
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import utils.AVOSUtils

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/19/15
 */
class InfoController @Inject() (
    val messagesApi: MessagesApi,
    infoService: InfoService) extends BaseController {

  def feedback = SecuredAction.async(parse.json) { implicit request =>
    (request.body \ "feedback").asOpt[String] match {
      case Some(fb) => infoService.addFeedback(Feedback(None, request.userId, fb)).map { _ =>
        success(Messages("success.feedback"))
      }
      case None => Future.successful(error(4027, Messages("invalid.feedback")))
    }
  }

  def installation = SecuredAction.async(parse.json) { implicit request =>
    val tokenOpt = (request.body \ "device_token").asOpt[String]
    val typeOpt = (request.body \ "device_type").asOpt[String]

    (tokenOpt, typeOpt) match {
      case (Some(token), Some(t)) => infoService.findInstallation(t, request.userId).flatMap {
        case Some(installation) =>
          val future = if (installation.deviceToken != token) {
            AVOSUtils.installations(token, t).flatMap { objectId =>
              infoService.updateInstallation(installation.id.get, installation.copy(objectId = objectId, userId = request.userId, status = 1, updated = System.currentTimeMillis() / 1000))
            }
          } else {
            infoService.updateInstallation(installation.id.get, installation.copy(userId = request.userId, status = 1, updated = System.currentTimeMillis() / 1000))
          }
          future.map(_ => success(Messages("success.install")))
        case None =>
          AVOSUtils.installations(token, t).flatMap { objectId =>
            infoService.insertInstallation(Installation(None, request.userId, objectId, token, t, 1))
              .map(_ => success(Messages("success.install")))
          }
      }
      case _ => Future.successful(error(4028, Messages("invalid.installation")))
    }
  }

}
