package com.likeorz.actors

import akka.actor.{Props, ActorLogging, Actor}
import com.likeorz.utils.MongoDB
import com.typesafe.config.ConfigFactory

class EventActor extends Actor with ActorLogging {
  import EventActor._

  override def preStart() = {
    val conf = ConfigFactory.load("application")
    val producers = conf.getString("event-producer.address").split(",").filterNot(_.isEmpty)
    // Discovery remote event producer
    log.warning("Looking up remote event producers and register...")
    producers.foreach { address =>
      val ref = context.actorSelection(s"akka.tcp://like-api-server@$address/user/event-producer-actor")
      ref ! JOIN
    }
  }

  override def receive = {
    case PING_FROM_PRODUCER =>
      log.info(s"Receive ping from event producer[${sender()}}]")
      sender() ! JOIN
    case msg: String =>
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
  val JOIN = "JOIN"
  val PING_FROM_PRODUCER = "PING_FROM_PRODUCER"
  val props = Props[EventActor]
}
