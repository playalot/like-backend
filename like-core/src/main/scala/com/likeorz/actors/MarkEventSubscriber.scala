package com.likeorz.actors

import javax.inject.Inject

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.LikeEvent
import com.likeorz.models.TimelineFeed
import com.likeorz.services.{ MongoDBService, TagService }
import com.likeorz.utils.{ GlobalConstants, KeyUtils, RedisCacheClient }

import scala.concurrent.{ ExecutionContext, Future }

class MarkEventSubscriber @Inject() (tagService: TagService, mongoDBService: MongoDBService) extends Actor with ActorLogging {

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
        val timestamp = System.currentTimeMillis() / 1000

        // Send feed to tag marker
        if (!mongoDBService.postInTimelineForUser(postId, userId)) {
          val markFeed = TimelineFeed(postId, TimelineFeed.TypeBasedOnTag, tag = Some(tagName), ts = timestamp)
          mongoDBService.insertTimelineFeedForUser(markFeed, userId)
        }

        tagService.getTagByName(tagName).flatMap {
          case Some(tag) =>
            if (tag.usage > GlobalConstants.MinTagUsage) {
              tagService.getUserIdsForTag(tag.id.get).map { userIds =>
                log.debug("subscribers[" + tag.name + "]: " + userIds.take(10).mkString("", ",", s"...(${userIds.size})"))
                userIds.foreach { uId =>
                  // Check if it is a active user
                  if (RedisCacheClient.zscore(KeyUtils.activeUsers, uId.toString).isDefined) {
                    if (!mongoDBService.postInTimelineForUser(postId, uId)) {
                      val markFeed = TimelineFeed(postId, TimelineFeed.TypeBasedOnTag, tag = Some(tag.name), ts = timestamp)
                      mongoDBService.insertTimelineFeedForUser(markFeed, uId)
                    }
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
