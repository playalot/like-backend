package com.likeorz.actors

import javax.inject.Inject

import akka.actor.{ Actor, ActorLogging }
import cn.jpush.api.JPushClient
import cn.jpush.api.common.resp.{ APIRequestException, APIConnectionException }
import cn.jpush.api.push.model.audience.Audience
import cn.jpush.api.push.model.notification.{ IosNotification, Notification }
import cn.jpush.api.push.model.{ Platform, PushPayload }
import com.likeorz.push.{ JPushNotification, PushNotification }
import com.likeorz.utils.AVOSUtils
import play.api.{ Configuration, Logger }

class PushNotificationActor @Inject() (configuration: Configuration) extends Actor with ActorLogging {

  val masterSecret = configuration.getString("jpush.secretKey").get
  val appKey = configuration.getString("jpush.appKey").get

  val jpushClient = new JPushClient(masterSecret, appKey, 3)

  def receive = {
    case msg: String => log.info(s"PushNotificationActor received message '$msg'")
    case n: PushNotification =>
      AVOSUtils.pushNotification(n.targetId, n.alert, n.badge, n.extra)
      ()
    case n: JPushNotification =>
      val notification = if (n.extra.isEmpty) {
        Notification.newBuilder()
          .addPlatformNotification(IosNotification.newBuilder().setAlert(n.alert).setBadge(n.badge).build())
          .build()
      } else {
        import collection.JavaConversions._
        Notification.newBuilder()
          .addPlatformNotification(IosNotification.newBuilder()
            .setAlert(n.alert)
            .setBadge(n.badge)
            .addExtras(n.extra)
            .build())
          .build()
      }
      val payload = if (n.userIds.nonEmpty) {
        PushPayload.newBuilder()
          .setPlatform(Platform.android_ios())
          .setAudience(Audience.alias(n.userIds.toSeq: _*))
          .setNotification(notification)
          .build()
      } else if (n.tags.nonEmpty) {
        PushPayload.newBuilder()
          .setPlatform(Platform.android_ios())
          .setAudience(Audience.tag(n.tags.toSeq: _*))
          .setNotification(notification)
          .build()
      } else {
        PushPayload.alertAll(n.alert)
      }

      try {
        val result = jpushClient.sendPush(payload)
        log.debug(result.toString)
        Logger.debug(result.toString)
      } catch {
        case e: APIConnectionException => log.error("Connection error, should retry later:" + e.getMessage)
        case e: APIRequestException    => log.error("Should review the error, and fix the request: " + "[" + e.getErrorCode() + "]" + e.getErrorMessage)
        case e: Throwable              => log.error(e.getMessage)
      }
    case _ => log.error("Invalid message!")
  }
}

