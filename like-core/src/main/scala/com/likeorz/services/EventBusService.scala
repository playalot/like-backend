package com.likeorz.services

import javax.inject.{ Named, Inject, Singleton }

import akka.actor.ActorRef
import com.likeorz.event._

@Singleton
class EventBusService @Inject() (
    @Named("event-log-subscriber") eventLogSubscriber: ActorRef,
    @Named("publish-event-subscriber") publishSubscriber: ActorRef,
    @Named("mark-event-subscriber") markSubscriber: ActorRef,
    @Named("recommend-to-all-event-subscriber") recommendToAllSubscriber: ActorRef,
    @Named("recommend-to-group-event-subscriber") recommendToGroupSubscriber: ActorRef) {

  private val eventBus = new LikeEventBus

  // Event Logger subscribe all events
  eventBus.subscribe(eventLogSubscriber, "")
  // Like event subscribers
  eventBus.subscribe(publishSubscriber, LikeEventType.publish)
  eventBus.subscribe(markSubscriber, LikeEventType.mark)
  eventBus.subscribe(markSubscriber, LikeEventType.removeMark)
  eventBus.subscribe(recommendToGroupSubscriber, LikeEventType.recommendToGroup)
  eventBus.subscribe(recommendToAllSubscriber, LikeEventType.recommendToAll)

  def publish(event: LikeEvent) = {
    eventBus.publish(event)
  }

}
