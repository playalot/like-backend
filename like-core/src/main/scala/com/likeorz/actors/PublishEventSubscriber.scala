package com.likeorz.actors

import javax.inject.Inject

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.LikeEvent
import com.likeorz.models.TimelineFeed
import com.likeorz.services.TagService
import com.likeorz.services.store.MongoDBService
import com.likeorz.utils.{ FutureUtils, GlobalConstants, KeyUtils, RedisCacheClient }

import scala.concurrent.ExecutionContext

class PublishEventSubscriber @Inject() (tagService: TagService, mongoDBService: MongoDBService) extends Actor with ActorLogging {

  implicit val blockingContext = new ExecutionContext {

    override def execute(runnable: Runnable): Unit = runnable.run()

    override def reportFailure(cause: Throwable): Unit = log.error(cause.getMessage)
  }

  override def receive: Receive = {
    case event: LikeEvent =>

      FutureUtils.timedFuture("process publish event") {

        log.debug("publish a new post " + event)
        log.debug(context.self.toString())

        val postId = event.targetEntityId.get.toLong
        val userId = event.entityId.toLong
        val tags = (event.properties \ "tags").as[List[String]]
        val timestamp = System.currentTimeMillis() / 1000

        // Send feed to publisher
        if (!mongoDBService.postInTimelineForUser(postId, userId)) {
          val myPostFeed = TimelineFeed(postId, TimelineFeed.TypeMyPost, ts = timestamp)
          mongoDBService.insertTimelineFeedForUser(myPostFeed, userId)
        }

        // Send feed to tag subscribers
        FutureUtils.seqFutures(tags)(tagName => tagService.getTagByName(tagName))
          .map(tagOptList => tagOptList.flatten.filter(_.usage > GlobalConstants.MinTagUsage).sortBy(_.usage).reverse)
          .flatMap { filteredTags =>
            log.debug("filtered tags: " + filteredTags.map(_.name).mkString(","))
            FutureUtils.seqFutures(filteredTags) { tag =>
              tagService.getSubscriberIdsForTag(tag.id.get).map { userIds =>
                log.debug("subscribers[" + tag.name + "]: " + userIds.take(10).mkString("", ",", s"...(${userIds.size}})"))
                userIds.foreach { uId =>
                  // Check if it is a active user
                  if (RedisCacheClient.zscore(KeyUtils.activeUsers, uId.toString).isDefined) {
                    if (!mongoDBService.postInTimelineForUser(postId, uId)) {
                      val publishFeed = TimelineFeed(postId, TimelineFeed.TypeBasedOnTag, tag = Some(tag.name), ts = timestamp)
                      mongoDBService.insertTimelineFeedForUser(publishFeed, uId)
                    }
                  }
                }
              }
            }
          }
      }

    case _ => log.error("Invalid message")
  }

}
