package com.likeorz.event

import akka.event.EventBus
import akka.actor.ActorRef
import akka.event.SubchannelClassification
import akka.util.Subclassification

class LikeEventBus extends EventBus with SubchannelClassification {

  override type Classifier = String
  override type Event = LikeEvent
  override type Subscriber = ActorRef

  override protected def publish(event: Event, subscriber: Subscriber): Unit = {
    subscriber ! event
  }

  override protected def classify(event: Event): Classifier = event.eventType

  override protected implicit def subclassification: Subclassification[Classifier] = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier): Boolean = x == y
    def isSubclass(x: Classifier, y: Classifier): Boolean = x.startsWith(y)
  }

}
