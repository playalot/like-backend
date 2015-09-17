package com.likeorz.actors

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.LikeEvent

class EventLogSubscriber extends Actor with ActorLogging {

  override def receive: Receive = {
    case event: LikeEvent => log.debug("log this event " + event)
    case _                => log.error("Invalid message")
  }

}
