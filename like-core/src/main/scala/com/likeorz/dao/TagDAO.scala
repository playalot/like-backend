package com.likeorz.dao

import com.likeorz.models.{ Tag => Tg, Like, Comment, Mark }
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

trait TagsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class TagsTable(tag: Tag) extends Table[Tg](tag, "tag") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("tag")
    def likes = column[Long]("likes")
    def usage = column[Long]("usage")
    override def * = (id.?, name, likes, usage) <> (Tg.tupled, Tg.unapply _)
  }

  protected val tags = TableQuery[TagsTable]
}

trait MarksComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class MarksTable(tag: Tag) extends Table[Mark](tag, "mark") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def postId = column[Long]("post_id")
    def tagId = column[Long]("tag_id")
    def tagName = column[String]("tag")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")
    def updated = column[Long]("updated")
    def likes = column[Long]("likes")
    override def * = (id.?, postId, tagId, tagName.?, userId, created, updated, likes) <> (Mark.tupled, Mark.unapply _)
  }

  protected val marks = TableQuery[MarksTable]
}

trait LikesComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class LikesTable(tag: Tag) extends Table[Like](tag, "like") {
    def markId = column[Long]("mark_id")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")
    override def * = (markId, userId, created) <> (Like.tupled, Like.unapply _)
  }

  protected val likes = TableQuery[LikesTable]
}

trait CommentsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class CommentsTable(tag: Tag) extends Table[Comment](tag, "comment") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def markId = column[Long]("mark_id")
    def userId = column[Long]("user_id")
    def replyId = column[Long]("reply_id")
    def content = column[String]("content")
    def created = column[Long]("created")
    def place = column[String]("place")
    override def * = (id.?, markId, userId, replyId.?, content, created, place.?) <> (Comment.tupled, Comment.unapply _)
  }

  protected val comments = TableQuery[CommentsTable]
}
