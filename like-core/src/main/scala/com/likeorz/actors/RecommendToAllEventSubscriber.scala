package com.likeorz.actors

import javax.inject.Inject

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.LikeEvent
import com.likeorz.models.TimelineFeed
import com.likeorz.services.store.MongoDBService
import com.likeorz.utils.{ KeyUtils, RedisCacheClient }

class RecommendToAllEventSubscriber @Inject() (mongoDBService: MongoDBService) extends Actor with ActorLogging {

  override def receive: Receive = {
    case event: LikeEvent =>
      log.info("Editor pick: send to all active users")

      val postId = event.entityId.toLong

      // Remove all previous post from timeline
      mongoDBService.removeTimelineFeedByPostId(postId)

      RedisCacheClient.zrange(KeyUtils.activeUsers, 0, Long.MaxValue).foreach { userId =>
        val editorPickFeed = TimelineFeed(postId, TimelineFeed.TypeEditorPick)
        mongoDBService.insertTimelineFeedForUser(editorPickFeed, userId.toLong)
      }
    case _ => log.error("Invalid message")
  }

}
