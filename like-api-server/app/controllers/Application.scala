package controllers

import javax.inject.{ Named, Inject }

import akka.actor.{ ActorRef, ActorPath, ActorSystem }
import akka.pattern.ask
import com.likeorz.common.ApiServerRemoteCount
import com.likeorz.event.LikeEvent
import com.likeorz.models.{ User, Notification }
import play.api._
import play.api.i18n.{ Lang, Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import services.{ PushService, MarkService, NotificationService, UserService }
import utils.{ AVOSUtils, RedisCacheClient, MemcachedCacheClient }

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._

class Application @Inject() (
    system: ActorSystem,
    @Named("event-producer-actor") eventProducerActor: ActorRef,
    @Named("classification-actor") classificationActor: ActorRef,
    val messagesApi: MessagesApi,
    userService: UserService,
    markService: MarkService,
    pushService: PushService,
    notificationService: NotificationService) extends BaseController {

  def index = Action { implicit request =>

    Logger.debug("Languages: " + request.acceptLanguages.map(_.code).mkString(", "))

    Logger.debug(Messages("invalid.sessionToken")(Messages(Lang("en"), messagesApi)))

    Logger.debug(Messages("invalid.mobileCode"))

    //    var preprocessStart = System.nanoTime()
    //    var response = Await.result(classificationActor.ask(com.likeorz.common.Tags(Seq("明日香", "EVA", "景品", "傲娇么么哒", "其实是晒钱包的！", "小恶魔")))(10.second).map(_.asInstanceOf[Int]), 20.seconds)
    //    var preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
    //    println(response + ":" + preprocessElapsed + "ms")
    //    preprocessStart = System.nanoTime()
    //    response = Await.result(classificationActor.ask(com.likeorz.common.Tags(Seq("高达")))(10.second).map(_.asInstanceOf[Int]), 20.seconds)
    //    preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
    //    println(response + ":" + preprocessElapsed + "ms")
    //    preprocessStart = System.nanoTime()
    //    response = Await.result(classificationActor.ask(com.likeorz.common.Tags(Seq("美食")))(10.second).map(_.asInstanceOf[Int]), 20.seconds)
    //    preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
    //    println(response + ":" + preprocessElapsed + "ms")
    //    eventProducerActor ! "PING"

    //    val remotePushActor = system.actorSelection(s"akka.tcp://LikeClusterSystem@127.0.0.1:2552/user/EventActor")

    //    val event = LikeEvent(None, "test", "user", "123", None, None, Json.obj("test" -> "111", "field1" -> "value1"))
    //
    //    import com.likeorz.event.LikeEvent.likeEventFormat
    //
    //    println(Json.toJson(event).toString())

    //    remotePushActor ! Json.toJson(event).toString()

    //    val hgg = "5542ea3be4b0679ef5cb6190"
    //    val gg = "55573daee4b076f1c3914798"
    //    val gj = "5541d612e4b0679ef5c27b8e"
    //    println(Json.obj("alert" -> "Like test notification", "badge" -> 5).deepMerge(Json.obj("badge" -> 0, "userId" -> 715, "test" -> "test")))

    //    AVOSUtils.pushNotification("")

    //    userService.findById(1).map(println)

    //    userService.findByMobileAndZone("66666688868", 86).map(println)

    //    userService.count().map(println)

    //    pushNotificationService.sendNotification(Notification(None, "LIKE", 715L, 715L, 1234, Some("aaa"), Some(123L)))

    //    MemcachedCacheClient.save[String]("session_user:e5b7f1ef625fc31c62a6577e71bb9ac1d2491177d1b8bee9d4db4b72ef177014", "715", 900000)
    //    MemcachedCacheClient.save[String]("session_user:187", "187", 900000)
    //    MemcachedCacheClient.save[String]("session_user:715", "715", 900000)
    //    MemcachedCacheClient.save[String]("session_user:3214", "3214", 900000)

    //    userService.getUserInfo(715).map(println)
    //    userService.getUserInfo(187).map(println)

    //    println(Json.prettyPrint(Json.toJson(RedisCacheClient.hgetAll(KeyUtils.user(715)))))
    //    println(Json.prettyPrint(Json.toJson(RedisCacheClient.hgetAll(KeyUtils.user(187)))))

    //    println(MemcachedCacheClient.find[Long]("session_user:e5b7f1ef625fc31c62a6577e71bb9ac1d2491177d1b8bee9d4db4b72ef177014"))

    //    println(RedisCacheClient.hget("sdf", "111"))
    //    println(RedisCacheClient.hmget("12312", "nickname", "avatar", "cover", "likes"))
    //
    //    userService.getUserInfo(1234).map(println)
    //    userService.getUserInfo(7150).map(println)
    //    RedisCacheClient.sAdd("test_seen", "4", "5", "6")
    //    println(RedisCacheClient.sMembers("test_seen"))

    //    for (i <- 1 to 100000) {
    //      userService.insert(User(None, None, None, "", "user_" + i, "default_avatar.jpg", "default_cover.jpg"))
    //    }

    //    userService.countFollowers(715).map(println)
    //    userService.countFollowings(715).map(println)
    //    userService.countFollowers(71500).map(println)
    //

    //    val t0 = System.currentTimeMillis()
    //
    //    val f1 = (1 to 100000).map(i => userService.getNickname(i))
    //    println("Pre:" + (System.currentTimeMillis() - t0))
    //    Future.successful(System.currentTimeMillis()).flatMap { t1 =>
    //      Future.sequence(f1).map { rs =>
    //        rs
    //        val elapsedMs1 = (System.currentTimeMillis() - t1)
    //        println("F1: " + elapsedMs1 + "ms")
    //
    //      }
    //    }

    //    val t2 = System.currentTimeMillis()
    //    for (i <- 1 to 100) (Await.result(Future.sequence(f2), 30.seconds))
    //    val elapsedMs2 = (System.currentTimeMillis() - t2)
    //    println("F2: " + elapsedMs2 + "ms")

    //    userService.findByMobile("18910438864").map { user =>
    //      userService.insert(user.get.copy(id = None, mobile = (user.get.mobile.toLong + 3).toString)).map(println)
    //      userService.count().map(println)
    //    }

    //    markService.rebuildMarkCache()
    //    markService.exportPostWithTags()
    //    markService.exportLikes()
    Ok(Json.obj("status" -> "1.1.0"))
  }

  def test = SecuredAction { implicit request =>
    Logger.error(s"User[$request.userId] run test!!!")
    //    markService.rebuildMarkCache()
    //    markService.rebuildUserLikesCache()
    //    markService.rebuildUserCountsCache()
    Ok("UserId: " + request.userId)
  }

  def status = UserAwareAction { implicit request =>
    val remotes = try {
      Await.result(eventProducerActor.ask(ApiServerRemoteCount)(1.second).map(_.asInstanceOf[Int]), 2.seconds)
    } catch {
      case _: Throwable => 0
    }
    Ok(Json.obj(
      "version" -> "1.1.0",
      "nf-event-actor" -> remotes
    ))
  }

}