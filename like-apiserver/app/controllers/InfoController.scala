package controllers

import javax.inject.Inject

import models.Feedback
import services.InfoService
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._

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

}
