package services

import models.{ Installation, Feedback }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/19/15
 */
trait InfoService {

  def addFeedback(fb: Feedback): Future[Feedback]

  def findInstallation(deviceType: String, userId: Long): Future[Option[Installation]]

  def insertInstallation(installation: Installation): Future[Installation]

  def updateInstallation(id: Long, installation: Installation): Future[Unit]

}
