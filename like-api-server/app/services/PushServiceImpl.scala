package services

import javax.inject.{ Inject, Singleton }

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.routing.RoundRobinPool
import com.likeorz.actors.PushNotificationActor
import com.likeorz.dao.InstallationComponent
import com.likeorz.push.{ JPushNotification, PushNotification }
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ JsObject, Json }
import play.api.{ Logger, Play }
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/23/15
 */
@Singleton
class PushServiceImpl @Inject() (system: ActorSystem, protected val dbConfigProvider: DatabaseConfigProvider)
    extends PushService
    with InstallationComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  //  val hostname = Play.current.configuration.getString("push-actor.hostname").get
  //  val port = Play.current.configuration.getInt("push-actor.port").get
  //  val remotePushActor = system.actorSelection(s"akka.tcp://LikeActorSystem@$hostname:$port/user/PushNotificationActor")

  val pushActorPool: ActorRef = system.actorOf(RoundRobinPool(5).props(Props[PushNotificationActor]), "pushActorPool")

  override def sendPushNotification(notification: PushNotification): Unit = {
    //remotePushActor ! push
    Logger.debug("Send Avos: " + notification)
    pushActorPool ! notification
  }

  override def sendPushNotificationViaJPush(notification: JPushNotification): Unit = {
    Logger.debug("Send JPush: " + notification)
    pushActorPool ! notification
  }

  override def sendPushNotificationToUser(userId: Long, alert: String, badge: Int, extra: JsObject = Json.obj()): Future[Unit] = {
    db.run(installations.filter(i => i.deviceType === "ios" && i.userId === userId).result.headOption).map {
      case Some(install) => sendPushNotification(PushNotification(install.objectId, alert, badge, extra))
      case None          => ()
    }
  }

}
