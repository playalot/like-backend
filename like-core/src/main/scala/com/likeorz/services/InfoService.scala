package com.likeorz.services

import com.google.inject.Inject
import com.likeorz.dao._
import com.likeorz.models._
import slick.driver.JdbcProfile
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class InfoService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
    with FeedbackComponent with InstallationComponent with ReportsComponent {

  import driver.api._

  def addFeedback(fb: Feedback): Future[Feedback] = {
    db.run(feedback returning feedback.map(_.id) += fb).map(id => fb.copy(id = Some(id)))
  }

  def listFeedbacks(pageSize: Int, page: Int): Future[Seq[Feedback]] = {
    db.run(feedback.sortBy(_.created.desc).drop(pageSize * page).take(pageSize).result)
  }

  def deleteFeedback(fbId: Long): Future[Int] = {
    db.run(feedback.filter(_.id === fbId).delete)
  }

  def addReport(report: Report): Future[Report] = {
    db.run(reports returning reports.map(_.id) += report).map(id => report.copy(id = Some(id)))
  }

  def listReports(pageSize: Int, page: Int): Future[Seq[Report]] = {
    db.run(reports.sortBy(_.created.desc).drop(pageSize * page).take(pageSize).result)
  }

  def deleteReport(rpId: Long): Future[Int] = {
    db.run(reports.filter(_.id === rpId).delete)
  }

  def findInstallation(deviceType: String, userId: Long): Future[Option[Installation]] = {
    db.run(installations.filter(i => i.deviceType === deviceType && i.userId === userId).result.headOption)
  }

  def insertInstallation(installation: Installation): Future[Installation] = {
    db.run(installations returning installations.map(_.id) += installation).map(id => installation.copy(id = Some(id)))
  }

  def updateInstallation(id: Long, installation: Installation): Future[Unit] = {
    db.run(installations.filter(_.id === id).update(installation)).map(_ => ())
  }

}
