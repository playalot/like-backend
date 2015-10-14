package com.likeorz.actors

import javax.inject.{ Inject, Singleton }

import akka.actor.{ Actor, ActorLogging, Props }
import com.likeorz.common.PushUnreadLikes
import com.likeorz.push.JPushNotification
import com.likeorz.utils.{ FutureUtils, RedisCacheClient, KeyUtils }
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
      FutureUtils.seqFutures(RedisCacheClient.zrevrangeWithScores(KeyUtils.newLikes, 0, Long.MaxValue)) { kv =>
        val userId = kv._1
        val newLikes = kv._2.toInt
        RedisCacheClient.zrem(KeyUtils.newLikes, userId)
        notificationService.countForUser(userId.toLong).flatMap { countTotal =>
          if (countTotal > 0 && newLikes > 0) {
            userService.syncDBLikesFromCache(userId.toLong).flatMap { _ =>
              pushService.sendPushNotificationViaJPush(JPushNotification(List(userId), List(), s"你收到了${newLikes}个新的赞,快来看看", countTotal))
              pushService.sendPushNotificationToUser(userId.toLong, s"你收到了${newLikes}个赞,快来看看", countTotal)
            }
          } else {
            Future.successful(())
          }
        }
      }
    case _ => log.error("Invalid message!")
  }

}

object PushNewLikesActor {
  val props = Props[PushNewLikesActor]
}
