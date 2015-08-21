package com.likeorz.dao

import com.likeorz.models._
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

trait PostsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class PostsTable(tag: Tag) extends Table[Post](tag, "post") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def `type` = column[String]("type")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")
    def updated = column[Long]("updated")
    def place = column[String]("place")
    def location = column[String]("location")
    def description = column[String]("description")
    def score = column[Int]("score")
    override def * = (id.?, content, `type`, userId, created, updated, place.?, location.?, description.?, score.?) <> (Post.tupled, Post.unapply _)
  }

  protected val posts = TableQuery[PostsTable]
}

trait ReportsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class ReportsTable(tag: Tag) extends Table[Report](tag, "report") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def postId = column[Long]("post_id")
    def created = column[Long]("created")
    def reason = column[String]("reason")
    override def * = (id.?, userId, postId, created, reason.?) <> (Report.tupled, Report.unapply _)
  }

  protected val reports = TableQuery[ReportsTable]
}

trait RecommendsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class RecommendsTable(tag: Tag) extends Table[Recommend](tag, "recommend") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def postId = column[Long]("post_id")
    def created = column[Long]("created")
    override def * = (id.?, postId, created) <> (Recommend.tupled, Recommend.unapply _)
  }

  protected val recommends = TableQuery[RecommendsTable]
}

trait DeletedPhotosComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class DeletedPhotosTable(tag: Tag) extends Table[DeletedPhoto](tag, "del_photo") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def photo = column[String]("photo")
    def created = column[Long]("created")
    override def * = (id.?, photo, created) <> (DeletedPhoto.tupled, DeletedPhoto.unapply _)
  }

  protected val deletes = TableQuery[DeletedPhotosTable]
}
