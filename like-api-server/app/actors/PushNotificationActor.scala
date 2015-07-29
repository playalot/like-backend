package com.likeorz.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.likeorz.push.PushNotification
import com.likeorz.utils.AVOSUtils

class PushNotificationActor extends Actor with ActorLogging {

  def receive = {
    case msg: String => log.info(s"PushNotificationActor received message '$msg'")
    case n: PushNotification =>
      log.debug("Send: " + n)
      AVOSUtils.pushNotification(n.targetId, n.alert, n.badge, n.extra)
      ()
    case _ => log.error("Invalid message!")
  }
}

object PushNotificationActor {
  val props = Props[PushNotificationActor]
}
