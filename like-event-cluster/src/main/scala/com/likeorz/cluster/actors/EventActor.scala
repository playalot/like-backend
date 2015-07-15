package com.likeorz.cluster.actors

import akka.actor.{ Props, ActorLogging, Actor }
import com.likeorz.cluster.utils.MongoDB
import com.likeorz.common.{ Ping, JoinApiServer, Event }

class EventActor extends Actor with ActorLogging {

  override def receive = {
    case Ping =>
      log.info(s"Receive ping from event producer[${sender()}}]")
      sender() ! JoinApiServer
    case Event(msg) =>
      println(msg)
      try {
        log.debug(msg)
        MongoDB.insertEventJson(msg)
        ()
      } catch {
        case e: Throwable => e.printStackTrace()
      }
    case _ =>
      log.error("Invalid message!")
  }

}

object EventActor {
  val props = Props[EventActor]
}
