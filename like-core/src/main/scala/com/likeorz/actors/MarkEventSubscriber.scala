package com.likeorz.actors

import javax.inject.Inject

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.LikeEvent
import com.likeorz.models.TimelineFeed
import com.likeorz.services.TagService
import com.likeorz.utils.{ GlobalConstants, KeyUtils, RedisCacheClient }
import play.api.libs.json.Json

import scala.concurrent.{ ExecutionContext, Future }

class MarkEventSubscriber @Inject() (tagService: TagService) extends Actor with ActorLogging {

  implicit val blockingContext = new ExecutionContext {

    override def execute(runnable: Runnable): Unit = runnable.run()

    override def reportFailure(cause: Throwable): Unit = log.error(cause.getMessage)
  }

  override def receive: Receive = {
    case event: LikeEvent =>

      timedFuture("process mark event") {
        log.debug("add mark to post " + event)

        val postId = event.targetEntityId.get.toLong
        val userId = event.entityId.toLong
        val tagName = (event.properties \ "tag").as[String]

        // Send feed to tag marker
        if (RedisCacheClient.sadd(KeyUtils.timelineIds(userId), Seq(postId.toString)) > 0) {
          // Add feed to timeline
          val myPostFeed = TimelineFeed(postId, TimelineFeed.TypeBasedOnTag, tag = Some(tagName))
          RedisCacheClient.zadd(KeyUtils.timeline(userId), System.currentTimeMillis() / 1000, Json.toJson(myPostFeed).toString())
        }

        tagService.getTagByName(tagName).flatMap {
          case Some(tag) =>
            if (tag.usage > GlobalConstants.MinTagUsage) {
              tagService.getUserIdsForTag(tag.id.get).map { userIds =>
                log.debug("subscribers[" + tag.name + "]: " + userIds.take(10).mkString("", ",", s"...(${userIds.size})"))
                userIds.foreach { uId =>
                  if (RedisCacheClient.sadd(KeyUtils.timelineIds(uId), Seq(postId.toString)) > 0) {
                    val myPostFeed = TimelineFeed(postId, TimelineFeed.TypeBasedOnTag, tag = Some(tag.name))
                    RedisCacheClient.zadd(KeyUtils.timeline(uId), System.currentTimeMillis() / 1000, Json.toJson(myPostFeed).toString())
                  }
                }
              }
            } else {
              Future.successful(())
            }
          case None => Future.successful(())
        }
      }

    case _ => log.error("Invalid message")
  }

}
