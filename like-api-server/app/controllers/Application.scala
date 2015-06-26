package controllers

import javax.inject.Inject

import com.likeorz.models.Notification
import play.api._
import play.api.i18n.{ Lang, Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc._
import services.{ PushService, MarkService, NotificationService, UserService }
import utils.{ AVOSUtils, RedisCacheClient, MemcachedCacheClient }

class Application @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService,
    markService: MarkService,
    pushService: PushService,
    notificationService: NotificationService) extends BaseController {

  def index = Action { implicit request =>

    Logger.debug("111111")

    Logger.debug(Messages("invalid.mobileCode"))

    //    val hgg = "5542ea3be4b0679ef5cb6190"
    //    val gg = "55573daee4b076f1c3914798"
    //    val gj = "5541d612e4b0679ef5c27b8e"
    //    println(Json.obj("alert" -> "Like test notification", "badge" -> 5).deepMerge(Json.obj("badge" -> 0, "userId" -> 715, "test" -> "test")))

    //    AVOSUtils.pushNotification("")

    //    userService.findById(1).map(println)

    //    userService.findByMobileAndZone("66666688868", 86).map(println)

    //    userService.count().map(println)

    //    pushNotificationService.sendNotification(Notification(None, "LIKE", 715L, 715L, 1234, Some("aaa"), Some(123L)))

    //    markService.rebuildMarkCache()

    //    MemcachedCacheClient.save[String]("session_user:e5b7f1ef625fc31c62a6577e71bb9ac1d2491177d1b8bee9d4db4b72ef177014", "715", 900000)
    //    MemcachedCacheClient.save[String]("session_user:e5b7f1ef625fc31c62a6577e71bb9ac1d2491177d1b8bee9d4db4b72ef177014", "128", 900000)
    //    MemcachedCacheClient.save[String]("session_user:420ca1ba7a736e2bed8e23c7e1a78eca", "826", 900000)

    //    println(MemcachedCacheClient.find[Long]("session_user:e5b7f1ef625fc31c62a6577e71bb9ac1d2491177d1b8bee9d4db4b72ef177014"))

    //    RedisCacheClient.sAdd("test_seen", "3")
    //    RedisCacheClient.sAdd("test_seen", "4", "5", "6")
    //    println(RedisCacheClient.sMembers("test_seen"))

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