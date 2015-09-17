package com.likeorz.actors

import javax.inject.Inject

import akka.actor.{ ActorLogging, Actor }
import com.likeorz.event.LikeEvent
import com.likeorz.models.TimelineFeed
import com.likeorz.services.TagService
import com.likeorz.utils.{ GlobalConstants, KeyUtils, RedisCacheClient }
import play.api.libs.json.Json

import scala.concurrent.{ Future, ExecutionContext }

class PublishEventSubscriber @Inject() (tagService: TagService) extends Actor with ActorLogging {

  implicit val blockingContext = new ExecutionContext {

    override def execute(runnable: Runnable): Unit = runnable.run()

    override def reportFailure(cause: Throwable): Unit = log.error(cause.getMessage)
  }

  override def receive: Receive = {
    case event: LikeEvent =>

      timedFuture("process publish event") {

        log.debug("publish a new post " + event)
        log.debug(context.self.toString())
        val postId = event.targetEntityId.get.toLong
        val userId = event.entityId.toLong
        val tags = (event.properties \ "tags").as[List[String]]

        // Send feed to publisher
        if (RedisCacheClient.sadd(KeyUtils.timelineIds(userId), Seq(postId.toString)) > 0) {
          // Add feed to timeline
          val myPostFeed = TimelineFeed(postId, TimelineFeed.TypeMyPost)
          RedisCacheClient.zadd(KeyUtils.timeline(userId), System.currentTimeMillis() / 1000, Json.toJson(myPostFeed).toString())
        }

        // Send feed to tag subscribers
        seqFutures(tags)(tagName => tagService.getTagByName(tagName))
          .map(tagOptList => tagOptList.flatten.filter(_.usage > GlobalConstants.MinTagUsage).sortBy(_.usage).reverse)
          .flatMap { filteredTags =>
            log.debug("filtered tags: " + filteredTags.map(_.name).mkString(","))
            seqFutures(filteredTags) { tag =>
              tagService.getUserIdsForTag(tag.id.get).map { userIds =>
                log.debug("subscribers[" + tag.name + "]: " + userIds.take(10).mkString("", ",", s"...(${userIds.size}})"))
                userIds.foreach { uId =>
                  if (RedisCacheClient.sadd(KeyUtils.timelineIds(uId), Seq(postId.toString)) > 0) {
                    val myPostFeed = TimelineFeed(postId, TimelineFeed.TypeBasedOnTag, tag = Some(tag.name))
                    RedisCacheClient.zadd(KeyUtils.timeline(uId), System.currentTimeMillis() / 1000, Json.toJson(myPostFeed).toString())
                  }
                }
              }
            }
          }
      }
    case _ => log.error("Invalid message")
  }

  private def seqFutures[T, U](items: TraversableOnce[T])(func: T => Future[U]): Future[List[U]] = {
    items.foldLeft(Future.successful[List[U]](Nil)) {
      (f, item) =>
        f.flatMap {
          x => func(item).map(_ :: x)
        }
    } map (_.reverse)
  }

}
