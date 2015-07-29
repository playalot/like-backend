package controllers

import javax.inject.Inject

import com.likeorz.models._
import com.likeorz.services.{ UserService, InfoService }
import com.likeorz.utils.AVOSUtils
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/19/15
 */
class InfoController @Inject() (
    val messagesApi: MessagesApi,
    infoService: InfoService,
    userService: UserService) extends BaseController {

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

}
