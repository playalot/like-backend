package com.likeorz.dao

import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile
import com.likeorz.models.{ Tag => T }

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait TagsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class TagsTable(tag: Tag) extends Table[T](tag, "tag") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def tagName = column[String]("tag")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")
    def updated = column[Long]("updated")
    def likes = column[Long]("likes")

    override def * = (id.?, tagName, userId, created, updated, likes) <> (T.tupled, T.unapply _)
  }
}

