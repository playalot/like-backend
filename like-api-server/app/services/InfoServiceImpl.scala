package services

import com.google.inject.Inject
import com.likeorz.dao._
import com.likeorz.models._
import slick.driver.JdbcProfile
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/19/15
 */
class InfoServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends InfoService
    with FeedbackComponent with InstallationComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val feedback = TableQuery[FeedbackTable]
  private val installations = TableQuery[InstallationTable]

  override def addFeedback(fb: Feedback): Future[Feedback] = {
    db.run(feedback returning feedback.map(_.id) += fb).map(id => fb.copy(id = Some(id)))
  }

  override def findInstallation(deviceType: String, userId: Long): Future[Option[Installation]] = {
    db.run(installations.filter(i => i.deviceType === deviceType && i.userId === userId).result.headOption)
  }

  override def insertInstallation(installation: Installation): Future[Installation] = {
    db.run(installations returning installations.map(_.id) += installation).map(id => installation.copy(id = Some(id)))
  }

  override def updateInstallation(id: Long, installation: Installation): Future[Unit] = {
    db.run(installations.filter(_.id === id).update(installation)).map(_ => ())
  }

}
