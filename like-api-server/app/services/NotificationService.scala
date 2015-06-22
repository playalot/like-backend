package services

import models.{ Post, User, Notification }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/1/15
 */
trait NotificationService {

  def insert(notification: Notification): Future[Notification]

  def countForUser(userId: Long): Future[Long]

  def getNotifications(userId: Long, timestamp: Option[Long] = None, pageSize: Int = 20): Future[Seq[(Notification, User, Option[(Post, User)])]]

}
