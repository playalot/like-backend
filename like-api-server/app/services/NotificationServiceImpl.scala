package services

import com.google.inject.Inject
import com.likeorz.models._
import com.likeorz.dao._
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile
import utils.RedisCacheClient

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/1/15
 */
class NotificationServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends NotificationService
    with NotificationsComponent with PostsComponent with UsersComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def insert(notification: Notification): Future[Notification] = {
    db.run(notifications returning notifications.map(_.id) += notification).map(id => notification.copy(id = Some(id)))
  }

  override def deleteLikeNotification(fromId: Long, postId: Long, tag: String): Future[Int] = {
    db.run(notifications.filter(n => n.`type` === "LIKE" && n.fromUserId === fromId && n.postId === postId && n.tagName === tag).delete)
  }

  override def countForUser(userId: Long): Future[Long] = {
    RedisCacheClient.zScore("user_notifies", userId.toString) match {
      case Some(ts) =>
        for {
          count <- db.run(notifications.filter(x => x.userId === userId && x.updated >= ts.toLong).length.result)
        } yield {
          count
        }
      case None =>
        RedisCacheClient.zAdd("user_notifies", System.currentTimeMillis / 1000, userId.toString)
        Future.successful(0)
    }
  }

  override def getNotifications(userId: Long, timestamp: Option[Long] = None, pageSize: Int = 30): Future[Seq[(Notification, User, Option[(Post, User)])]] = {
    // TODO optimize
    val query = if (timestamp.isDefined) {
      (for {
        ((notification, user), (postAndUser)) <- notifications join users on (_.fromUserId === _.id) joinLeft (posts join users on (_.userId === _.id)) on (_._1.postId === _._1.id)
        if notification.userId === userId && notification.updated < timestamp.get
      } yield (notification, user, postAndUser)).sortBy(_._1.updated.desc).take(pageSize)
    } else {
      (for {
        ((notification, user), (postAndUser)) <- notifications join users on (_.fromUserId === _.id) joinLeft (posts join users on (_.userId === _.id)) on (_._1.postId === _._1.id)
        if notification.userId === userId
      } yield (notification, user, postAndUser)).sortBy(_._1.updated.desc).take(pageSize)
    }
    RedisCacheClient.zAdd("user_notifies", System.currentTimeMillis / 1000, userId.toString)
    db.run(query.result)
  }

}
