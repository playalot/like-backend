package com.likeorz.services

import akka.event.EventBus
import com.likeorz.event._

trait EventBusService {
  val eventBus: EventBus

  def publish(event: LikeEvent)
}

