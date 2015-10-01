package controllers

import javax.inject.{ Named, Inject }

import akka.actor.{ Props, ActorRef, ActorPath, ActorSystem }
import akka.pattern.ask
import akka.util.Timeout
import com.likeorz.actors.{ EventLogSubscriber, PublishEventSubscriber, MarkEventSubscriber, RecommendToAllEventSubscriber }
import com.likeorz.common.{ PushUnreadLikes, ClassifyPost, ApiServerRemoteCount }
import com.likeorz.event.{ LikeEventType, LikeEventBus, LikeEvent }
import com.likeorz.models.{ MarkDetail, PostMarks, User, Notification }
import com.likeorz.push.JPushNotification
import com.likeorz.services.store.MongoDBService
import com.likeorz.utils.RedisCacheClient
import play.api._
import play.api.i18n.{ Lang, Messages, MessagesApi }
import play.api.libs.concurrent.Akka
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._
import com.likeorz.services._
import utils.MemcachedCacheClient

import scala.concurrent.{ Future, Await }
import scala.concurrent.duration._

class Application @Inject() (
    system: ActorSystem,
    @Named("classification-actor") classificationActor: ActorRef,
    val messagesApi: MessagesApi,
    userService: UserService,
    markService: MarkService,
    pushService: PushService,
    postService: PostService,
    tagService: TagService,
    eventBusService: EventBusService,
    mongoDBService: MongoDBService,
    notificationService: NotificationService) extends BaseController {

  def index = Action { implicit request =>

    Logger.debug("Languages: " + request.acceptLanguages.map(_.code).mkString(", "))

    Logger.debug(Messages("invalid.sessionToken")(Messages(Lang("en"), messagesApi)))

    Logger.debug(Messages("invalid.mobileCode"))

    //    mongoDBService.findPostsByTagIds(Seq(1334, 672, 59), 10, None)
    //    mongoDBService.removeTimelineFeedForUserWhenUnsubscribeTag(187, "高达")
    //    mongoDBService.insertPostMarks(PostMarks(1234, 1234, Seq(MarkDetail(111, 222, 123, "t1", Seq(111, 112)))))
    //    mongoDBService.insertMarkForPost(1234, MarkDetail(112, 222, 124, "t2", Seq(111, 112)))
    //    mongoDBService.findPostsByTags(Seq(123)).foreach(println)
    //    mongoDBService.findPostsByTags(Seq(124)).foreach(println)
    //    mongoDBService.findPostsByTags(Seq(126)).foreach(println)
    //    mongoDBService.deletePostMarks(1234)
    //    mongoDBService.likeMark(111, 1234, 555)
    //    mongoDBService.deleteMarkForPost(111, 1234)
    //    mongoDBService.unlikeMark(111, 1234, 555)
    //    eventBusService.publish(LikeEvent(None, "publish", "user", "1234", Some("post"), Some("1234"), properties = Json.obj("tags" -> Json.toJson(Seq("111", "222", "333")), "img" -> "123123.jpg")))

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
    //    mongoDBService.getFeedsFromTimelineForUser(715, 5, Some(1442838550)).foreach(println)
    //    mongoDBService.insertTimelineFeedForUser(TimelineFeed(1234, TimelineFeed.TypeEditorPick), 715)
    //    println(mongoDBService.postInTimelineForUser(1234, 715))
    //    println(mongoDBService.postInTimelineForUser(1235, 715))
    //    println(mongoDBService.postInTimelineForUser(1235, 716))

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

    //    userService.getUserInfo(715).map(x => println(x))

    //    userService.findByMobileAndZone("66666688868", 86).map(println)

    //    userService.count().map(println)

    //    val notification = JPushNotification(List("11226"), List(), "test from server2", 11)
    //    pushService.sendPushNotificationViaJPush(notification)

    //    pushService.sendPushNotificationToUser(715, "test", 10)
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

    Ok(Json.obj("status" -> "1.2.0"))
  }

  def test = SecuredAction { implicit request =>
    Ok("UserId: " + request.userId)
  }

  def status = UserAwareAction { implicit request =>
    Ok(Json.obj(
      "version" -> "1.2.0"
    ))
  }

}