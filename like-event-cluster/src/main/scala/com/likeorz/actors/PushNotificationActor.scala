package com.likeorz.actors

import akka.actor.{Actor, ActorLogging, Props}
import com.likeorz.models.Notification

class PushNotificationActor extends Actor with ActorLogging {
  import PushNotificationActor._

  def receive = {
  	case msg: String =>
      println(s"RemoteActor received message '$msg'")
    case n: Notification => println(n)
    case _ => log.error("Invalid message!")
  }
}

object PushNotificationActor {
  val props = Props[PushNotificationActor]
  case class PongMessage(text: String)
}
