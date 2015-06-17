package controllers

import javax.inject.Inject

import play.api._
import play.api.i18n.{ Lang, Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc._
import services.{ MarkService, NotificationService, UserService }
import play.api.libs.concurrent.Execution.Implicits._
import utils.MemcachedCacheClient

class Application @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService,
    markService: MarkService,
    notificationService: NotificationService) extends BaseController {

  def index = Action { implicit request =>

    Logger.debug("111111")

    Logger.debug(Messages("invalid.mobileCode"))

    //    userService.findById(1).map(println)

    //    userService.findByMobileAndZone("66666688868", 86).map(println)

    //    userService.count().map(println)

    //    markService.rebuildMarkCache()

    //        MemcachedCacheClient.save[String]("session_user:e5b7f1ef625fc31c62a6577e71bb9ac1d2491177d1b8bee9d4db4b72ef177014", "72", 900000)

    //    MemcachedCacheClient.save[String]("session_user:420ca1ba7a736e2bed8e23c7e1a78eca", "826", 900000)

    //    println(MemcachedCacheClient.find[Long]("session_user:e5b7f1ef625fc31c62a6577e71bb9ac1d2491177d1b8bee9d4db4b72ef177014"))

    println("Languages: " + request.acceptLanguages.map(_.code).mkString(", "))

    println(Messages("invalid.sessionToken")(Messages(Lang("en"), messagesApi)))

    //    userService.findByMobile("18910438864").map { user =>
    //      userService.insert(user.get.copy(id = None, mobile = (user.get.mobile.toLong + 3).toString)).map(println)
    //      userService.count().map(println)
    //    }

    Ok(Json.obj("status" -> "1.1.0"))
  }

  def test = SecuredAction { implicit request =>
    println(request.userId)
    Ok("UserId: " + request.userId)
  }

}