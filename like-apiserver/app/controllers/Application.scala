package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import play.api._
import play.api.i18n.Messages.Message
import play.api.i18n.{ Lang, Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc._
import services.UserService
import play.api.libs.concurrent.Execution.Implicits._
import utils.MemcachedCacheClient

class Application @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService) extends BaseController {

  def index = Action { implicit request =>

    Logger.debug("111111")

    Logger.debug(Messages("invalid.mobileCode"))

    userService.findById(1).map(println)

    userService.findByMobile("66666688868").map(println)

    userService.count().map(println)

    MemcachedCacheClient.save[Long]("123456", 1L, 900)

    println(MemcachedCacheClient.find[Long]("4a557c5d0e3a01b3dc3a9be42f6c0aa80cc6f1f519340e345ad0eba0e7454793"))

    println("Languages: " + request.acceptLanguages.map(_.code).mkString(", "))

    println(Messages("invalid.sessionToken")(Messages(Lang("en"), messagesApi)))

    //    userService.findByMobile("18910438864").map { user =>
    //      userService.insert(user.get.copy(id = None, mobile = (user.get.mobile.toLong + 3).toString)).map(println)
    //      userService.count().map(println)
    //    }

    Ok(Json.obj("status" -> "ok"))
  }

  def test = SecuredAction { implicit request =>
    println(request.userId)
    Ok("UserId: " + request.userId)
  }

}