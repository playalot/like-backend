package com.likeorz.dao

import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile
import com.likeorz.models._

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

trait InstallationComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class InstallationTable(tag: Tag) extends Table[Installation](tag, "installation") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def objectId = column[String]("objectId")
    def deviceToken = column[String]("deviceToken")
    def deviceType = column[String]("deviceType")
    def status = column[Int]("status")
    def created = column[Long]("created")
    def updated = column[Long]("updated")

    override def * = (id.?, userId, objectId, deviceToken, deviceType, status, created, updated) <> (Installation.tupled, Installation.unapply _)
  }
}