package com.likeorz.services

import javax.inject.{ Inject, Named, Singleton }

import akka.actor.{ ActorRef, ActorSystem }
import com.likeorz.dao.InstallationComponent
import com.likeorz.push.{ JPushNotification, PushNotification }
import play.api.Logger
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{ JsObject, Json }
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton
class PushService @Inject() (
  system: ActorSystem,
  @Named("push-notification-actor") pushNotificationActor: ActorRef,
  protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
    with InstallationComponent {

  import driver.api._

  def sendPushNotification(notification: PushNotification): Unit = {
    //remotePushActor ! push
    Logger.debug("Send Avos: " + notification)
    pushNotificationActor ! notification
  }

  def sendPushNotificationViaJPush(notification: JPushNotification): Unit = {
    Logger.debug("Send JPush: " + notification)
    // TODO
    //pushNotificationActor ! notification
  }

  def sendPushNotificationToUser(userId: Long, alert: String, badge: Int, extra: JsObject = Json.obj()): Future[Unit] = {
    db.run(installations.filter(i => i.deviceType === "ios" && i.userId === userId).result.headOption).map {
      case Some(install) => sendPushNotification(PushNotification(install.objectId, alert, badge, extra))
      case None          => ()
    }
  }

}
