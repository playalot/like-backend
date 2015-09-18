package com.likeorz.services

import com.google.inject.Inject
import com.likeorz.models._
import com.likeorz.dao._
import com.likeorz.utils.RedisCacheClient
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

class NotificationServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends NotificationService
    with NotificationsComponent with PostsComponent with UsersComponent with UserInfoComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def insert(notification: Notification): Future[Notification] = {
    db.run(notifications returning notifications.map(_.id) += notification).map(id => notification.copy(id = Some(id)))
  }

  override def deleteLikeNotification(fromId: Long, postId: Long, tag: String): Future[Int] = {
    db.run(notifications.filter(n => n.`type` === "LIKE" && n.fromUserId === fromId && n.postId === postId && n.tagName === tag).delete)
  }

  override def deleteAllNotificationForPost(postId: Long): Future[Int] = {
    db.run(notifications.filter(n => n.postId === postId).delete)
  }

  override def countForUser(userId: Long): Future[Int] = {
    RedisCacheClient.zscore("user_notifies", userId.toString) match {
      case Some(ts) =>
        for {
          count <- db.run(notifications.filter(x => x.userId === userId && x.updated >= ts.toLong).length.result)
        } yield {
          count
        }
      case None =>
        RedisCacheClient.zadd("user_notifies", System.currentTimeMillis / 1000, userId.toString)
        Future.successful(0)
    }
  }

  override def countUnreadLikesForUser(userId: Long): Future[Int] = {
    RedisCacheClient.zscore("user_notifies", userId.toString) match {
      case Some(ts) =>
        for {
          count <- db.run(notifications.filter(x => x.userId === userId && x.updated >= ts.toLong && x.`type` === "LIKE").length.result)
        } yield {
          count
        }
      case None =>
        RedisCacheClient.zadd("user_notifies", System.currentTimeMillis / 1000, userId.toString)
        Future.successful(0)
    }

  }

  override def getNotifications(userId: Long, timestamp: Option[Long] = None, pageSize: Int = 15): Future[Seq[(Notification, UserInfo, Option[(Post, UserInfo)])]] = {

    val q = if (timestamp.isDefined) {
      (for {
        notification <- notifications
        user <- userinfo
        if notification.fromUserId === user.id && notification.userId === userId && notification.updated < timestamp.get
      } yield (notification, user)).sortBy(_._1.updated.desc).take(pageSize)
    } else {
      (for {
        notification <- notifications
        user <- userinfo
        if notification.fromUserId === user.id && notification.userId === userId
      } yield (notification, user)).sortBy(_._1.updated.desc).take(pageSize)
    }

    RedisCacheClient.zadd("user_notifies", System.currentTimeMillis / 1000, userId.toString)
    //    var s = System.nanoTime()
    //    println(s)
    db.run(q.result).flatMap { notificationAndUser =>
      //      var e = (System.nanoTime() - s) / 1e9
      //      println("time:" + e)
      val postIds = notificationAndUser.flatMap(_._1.postId).toSet

      if (postIds.nonEmpty) {
        val queryPost = for {
          post <- posts if post.id inSet postIds
          user <- userinfo if post.userId === user.id
        } yield (post, user)
        //        s = System.nanoTime()
        db.run(queryPost.result).map { postAndUser =>
          //          e = (System.nanoTime() - s) / 1e9
          //          println("time:" + e)
          val postMap = postAndUser.map(x => x._1.id -> x).toMap
          notificationAndUser.map {
            case (notification, user) =>
              (notification, user, postMap.get(notification.postId))
          }
        }
      } else {
        Future.successful(notificationAndUser.map {
          case (notification, user) =>
            (notification, user, None)
        })
      }
    }
  }

}
