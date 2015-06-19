package dao

import models.Feedback
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

/**
 * Created by Guan Guan
 * Date: 6/19/15
 */
trait FeedbackComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class FeedbackTable(tag: Tag) extends Table[Feedback](tag, "feedback") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def feedback = column[String]("feedback")
    def created = column[Long]("created")

    override def * = (id.?, userId, feedback, created) <> (Feedback.tupled, Feedback.unapply _)
  }
}