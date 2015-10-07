package com.likeorz.actors

import javax.inject.{ Inject, Singleton }

import akka.actor.{ Actor, ActorLogging, Props }
import com.likeorz.common.PushUnreadLikes
import com.likeorz.push.JPushNotification
import com.likeorz.utils.{ RedisCacheClient, KeyUtils }
import com.likeorz.services.{ PushService, UserService, NotificationService }

import scala.concurrent.Future

@Singleton
class PushNewLikesActor @Inject() (
    notificationService: NotificationService,
    pushService: PushService,
    userService: UserService) extends Actor with ActorLogging {

  import java.util.concurrent.Executors
  import concurrent.ExecutionContext
  implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(1))

  def receive = {
    case PushUnreadLikes =>
      log.debug("Start push likes to users")
      Thread.sleep(scala.util.Random.nextInt(30) * 1000)
      seqFutures(RedisCacheClient.zrangebyscore(KeyUtils.newLikes, 0, Double.MaxValue)) { userId =>
        val newLikesOpt = RedisCacheClient.zscore(KeyUtils.newLikes, userId)
        if (newLikesOpt.isDefined) {
          val newLikes = newLikesOpt.get.toInt
          RedisCacheClient.zrem(KeyUtils.newLikes, userId)
          notificationService.countForUser(userId.toLong).flatMap { countTotal =>
            if (countTotal > 0 && newLikesOpt.get > 0) {
              userService.syncDBLikesFromCache(userId.toLong).flatMap { _ =>
                pushService.sendPushNotificationViaJPush(JPushNotification(List(userId), List(), s"你收到了${newLikes}个新的赞,快来看看", countTotal))
                pushService.sendPushNotificationToUser(userId.toLong, s"你收到了${newLikes}个赞,快来看看", countTotal)

              }
            } else {
              Future.successful(())
            }
          }
        } else {
          Future.successful(())
        }
      }
    case _ => log.error("Invalid message!")
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

object PushNewLikesActor {
  val props = Props[PushNewLikesActor]
}
