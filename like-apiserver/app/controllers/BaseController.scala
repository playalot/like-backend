package controllers

import play.api.i18n.{ Messages, I18nSupport }
import play.api.libs.json.Json
import play.api.mvc._
import utils.MemcachedCacheClient

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/24/15
 */
trait BaseController extends Controller with I18nSupport {

  case class SecuredRequest[B](userId: Long, request: Request[B]) extends WrappedRequest(request)

  case class UserAwareRequest[B](userId: Option[Long], request: Request[B]) extends WrappedRequest(request)

  object SecuredAction extends ActionBuilder[SecuredRequest] {

    override def invokeBlock[A](request: Request[A], block: (SecuredRequest[A]) => Future[Result]): Future[Result] = {
      request.headers.get("LIKE-SESSION-TOKEN")
        .flatMap(token => MemcachedCacheClient.findUserId("session_user:" + token))
        .map(userId => block(new SecuredRequest(userId, request)))
        .getOrElse(Future.successful(Ok(Json.obj(
          "code" -> 4016,
          "field" -> "LIKE-SESSION-TOKEN",
          "message" -> Messages("invalid.sessionToken")
        ))))
    }
  }

  object UserAwareAction extends ActionBuilder[UserAwareRequest] {

    override def invokeBlock[A](request: Request[A], block: (UserAwareRequest[A]) => Future[Result]): Future[Result] = {
      request.headers.get("LIKE-SESSION-TOKEN") match {
        case Some(token) => block(new UserAwareRequest(MemcachedCacheClient.findUserId("session_user:" + token), request))
        case None        => block(new UserAwareRequest(None, request))
      }
    }
  }

  def getScreenWidth(implicit request: Request[AnyContent]): Int = {
    request.headers.get("LIKE_SCREEN_WIDTH") match {
      case Some(width) => try {
        val w = width.toInt
        if (w > 1242) 1242
        else if (w < 320) 320
        else w
      } catch {
        case _: Throwable => 1242
      }
      case None => 1242
    }
  }

}
