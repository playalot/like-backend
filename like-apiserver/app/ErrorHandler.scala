package app

import javax.inject._

import play.api.http.DefaultHttpErrorHandler
import play.api._
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router
import scala.concurrent._

/**
 * Created by Guan Guan
 * Date: 5/24/15
 */
class ErrorHandler @Inject() (
    env: Environment,
    config: Configuration,
    sourceMapper: OptionalSourceMapper,
    router: Provider[Router]) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      //      Status(statusCode)("A client error occurred: " + message)
      Ok(Json.obj(
        "code" -> statusCode,
        "message" -> message
      ))
    )
  }

  override def onServerError(request: RequestHeader, exception: Throwable) = {
    exception.printStackTrace()
    Future.successful(
      //      InternalServerError("A server error occurred: " + exception.getMessage)
      Ok(Json.obj(
        "code" -> 4000,
        "message" -> exception.getMessage
      ))
    )
  }

}