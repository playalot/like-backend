package com.likeorz.actors

import javax.inject.Inject

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.{ LikeEventType, LikeEvent }
import com.likeorz.services.MongoDBService

class EventLogSubscriber @Inject() (mongoDBService: MongoDBService) extends Actor with ActorLogging {

  override def receive: Receive = {
    case event: LikeEvent =>
      if (event.eventType == LikeEventType.publish || event.eventType == LikeEventType.mark || event.eventType == LikeEventType.like) {
        log.debug("log this event " + event)
        try {
          mongoDBService.insertEvent(event)
          ()
        } catch {
          case e: Throwable => e.printStackTrace()
        }
      }
    case _ => log.error("Invalid message")
  }

}
