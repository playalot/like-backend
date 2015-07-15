package com.likeorz.cluster.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.likeorz.common.Notification

class PushNotificationActor extends Actor with ActorLogging {

  def receive = {
    case msg: String =>
      println(s"RemoteActor received message '$msg'")
    case n: Notification =>
      println(n)
    case _ =>
      log.error("Invalid message!")
  }
}

object PushNotificationActor {
  val props = Props[PushNotificationActor]
}
