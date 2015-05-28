package dao

import models.Comment
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */

trait TagsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class TagsTable(tag: Tag) extends Table[models.Tag](tag, "tag") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def tagName = column[String]("tag")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")
    def updated = column[Long]("updated")
    def likes = column[Long]("likes")

    override def * = (id, tagName, userId, created, updated, likes) <> (models.Tag.tupled, models.Tag.unapply _)
  }
}

trait CommentConponent {
  self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class CommentsTable(tag: Tag) extends Table[Comment](tag, "comment") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def postId = column[Long]("post_id")
    def tagId = column[Long]("tag_id")
    def userId = column[Long]("user_id")
    def replyId = column[Long]("reply_id")
    def comment = column[String]("comment")
    def created = column[Long]("created")
    def location = column[String]("location")

    override def * = (id.?, postId, tagId, userId, replyId.?, comment, created, location.?) <> (Comment.tupled, Comment.unapply _)
  }
}

