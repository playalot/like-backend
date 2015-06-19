package services

import com.google.inject.Inject
import dao._
import models.Feedback
import slick.driver.JdbcProfile
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/19/15
 */
class InfoServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends InfoService
    with FeedbackComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val feedback = TableQuery[FeedbackTable]

  override def addFeedback(fb: Feedback): Future[Feedback] = {
    db.run(feedback returning feedback.map(_.id) += fb).map(id => fb.copy(id = Some(id)))
  }

}
