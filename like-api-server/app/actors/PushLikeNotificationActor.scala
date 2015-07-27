package com.likeorz.actors

import javax.inject.{ Inject, Singleton }

import akka.actor.{ Actor, ActorLogging, Props }
import com.likeorz.common.PushUnreadLikes
import com.likeorz.utils.KeyUtils
import services.{ PushService, NotificationService }
import utils.RedisCacheClient

import scala.concurrent.Future

@Singleton
class PushLikeNotificationActor @Inject() (notificationService: NotificationService,
  pushService: PushService)
    extends Actor with ActorLogging {

  import java.util.concurrent.Executors
  import concurrent.ExecutionContext
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  def receive = {
    case PushUnreadLikes =>
      log.debug("Start push likes to users")
      Thread.sleep(scala.util.Random.nextInt(50000))
      RedisCacheClient.zrangebyscore(KeyUtils.pushLikes, 0, Double.MaxValue).foreach { userId =>
        for {
          countLikes <- notificationService.countUnreadLikesForUser(userId.toLong)
          countTotal <- notificationService.countForUser(userId.toLong)
          push <- if (countTotal > 0 && RedisCacheClient.zscore(KeyUtils.pushLikes, userId).isDefined) pushService.sendPushNotificationToUser(userId.toLong, s"你收到了${countLikes}个赞,快来看看", countTotal) else Future.successful(())
        } yield {
          RedisCacheClient.zrem(KeyUtils.pushLikes, userId)
        }
      }
    case _ => log.error("Invalid message!")
  }

  private def seqFutures[T, U](items: TraversableOnce[T])(yourfunction: T => Future[U]): Future[List[U]] = {
    items.foldLeft(Future.successful[List[U]](Nil)) {
      (f, item) =>
        f.flatMap {
          x => yourfunction(item).map(_ :: x)
        }
    } map (_.reverse)
  }

}

object PushLikeNotificationActor {
  val props = Props[PushLikeNotificationActor]
}
