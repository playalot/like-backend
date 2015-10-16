package com.likeorz.actors

import javax.inject.Inject

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.LikeEvent
import com.likeorz.models.TimelineFeed
import com.likeorz.services.store.MongoDBService

class RecommendToAllEventSubscriber @Inject() (mongoDBService: MongoDBService) extends Actor with ActorLogging {

  override def receive: Receive = {
    case event: LikeEvent =>
      log.info("Editor pick: send to all active users")

      val postId = event.entityId.toLong

      // Remove all previous post from timeline
      mongoDBService.removeTimelineFeedByPostId(postId)

      val editorPickFeed = TimelineFeed(postId, TimelineFeed.TypeEditorPick)
      mongoDBService.insertTimelineFeedForAllUsers(editorPickFeed)

    case _ => log.error("Invalid message")
  }

}
