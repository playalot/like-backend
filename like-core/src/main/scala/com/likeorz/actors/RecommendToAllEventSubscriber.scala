package com.likeorz.actors

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.LikeEvent
import com.likeorz.models.TimelineFeed
import com.likeorz.utils.{ KeyUtils, RedisCacheClient }
import play.api.libs.json.Json

class RecommendToAllEventSubscriber extends Actor with ActorLogging {

  override def receive: Receive = {
    case event: LikeEvent =>
      log.info("Editor pick: send to all active users")

      val postId = event.targetEntityId.get.toLong

      RedisCacheClient.zrange(KeyUtils.activeUsers, 0, Long.MaxValue).foreach { userId =>
        if (RedisCacheClient.sadd(KeyUtils.timelineIds(userId.toLong), Seq(postId.toString)) > 0) {
          val editorPickFeed = TimelineFeed(postId, TimelineFeed.TypeEditorPick)
          RedisCacheClient.zadd(KeyUtils.timeline(userId.toLong), System.currentTimeMillis() / 1000, Json.toJson(editorPickFeed).toString())
        }
      }
    case _ => log.error("Invalid message")
  }

}
