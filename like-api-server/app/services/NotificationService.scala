package services

import com.likeorz.models._

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/1/15
 */
trait NotificationService {

  def insert(notification: Notification): Future[Notification]

  def deleteLikeNotification(fromId: Long, postId: Long, tag: String): Future[Int]

  def deleteAllNotificationForPost(postId: Long): Future[Int]

  def countForUser(userId: Long): Future[Int]

  def countUnreadLikesForUser(userId: Long): Future[Int]

  def getNotifications(userId: Long, timestamp: Option[Long] = None, pageSize: Int = 20): Future[Seq[(Notification, User, Option[(Post, User)])]]

}
