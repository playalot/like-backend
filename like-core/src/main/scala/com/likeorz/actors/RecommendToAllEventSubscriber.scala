package com.likeorz.actors

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.LikeEvent

class RecommendToAllEventSubscriber extends Actor with ActorLogging {

  override def receive: Receive = {
    case event: LikeEvent => log.info("recommend to all users")
    case _                => log.error("Invalid message")
  }

}
