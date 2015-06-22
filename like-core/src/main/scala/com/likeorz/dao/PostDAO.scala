package com.likeorz.dao

import com.likeorz.models._
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait PostsComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class PostsTable(tag: Tag) extends Table[Post](tag, "post") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def description = column[String]("description")
    def `type` = column[String]("type")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")
    def updated = column[Long]("updated")
    def tagId = column[Long]("tag_id")
    def likes = column[Long]("likes")

    override def * = (id.?, content, description.?, `type`, userId, created, updated, tagId, likes) <> (Post.tupled, Post.unapply _)
  }

}

trait ReportsComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class ReportsTable(tag: Tag) extends Table[Report](tag, "report") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def postId = column[Long]("post_id")
    def num = column[Long]("num")
    def created = column[Long]("created")

    override def * = (id.?, userId, postId, num, created) <> (Report.tupled, Report.unapply _)
  }
}

trait RecommendsComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class RecommendsTable(tag: Tag) extends Table[Recommend](tag, "recommend") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def postId = column[Long]("post_id")
    def created = column[Long]("created")

    override def * = (id.?, postId, created) <> (Recommend.tupled, Recommend.unapply _)
  }
}