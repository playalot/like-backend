package dao

import models._
import play.api.db.slick.{ HasDatabaseConfig, DatabaseConfigProvider, HasDatabaseConfigProvider }
import slick.driver.JdbcProfile

trait FollowsComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class FollowsTable(tag: Tag) extends Table[Follow](tag, "follow") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def fromId = column[Long]("from_id")
    def toId = column[Long]("to_id")
    def both = column[Boolean]("both")
    def created = column[Long]("created")

    override def * = (id.?, fromId, toId, both, created) <> (Follow.tupled, Follow.unapply _)
  }
}

trait MarksComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class MarksTable(tag: Tag) extends Table[Mark](tag, "mark") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def postId = column[Long]("post_id")
    def tagId = column[Long]("tag_id")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")
    def updated = column[Long]("updated")
    def likes = column[Long]("likes")

    override def * = (id.?, postId, tagId, userId, created, updated, likes) <> (Mark.tupled, Mark.unapply _)
  }
}

trait CommentsConponent {
  self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class CommentsTable(tag: Tag) extends Table[Comment](tag, "comment") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def markId = column[Long]("mark_id")
    def userId = column[Long]("user_id")
    def replyId = column[Long]("reply_id")
    def content = column[String]("content")
    def created = column[Long]("created")
    def location = column[String]("location")

    override def * = (id.?, markId, userId, replyId.?, content, created, location.?) <> (Comment.tupled, Comment.unapply _)
  }
}

trait LikesComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class LikesTable(tag: Tag) extends Table[Like](tag, "like") {
    def markId = column[Long]("mark_id")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")

    override def * = (markId, userId, created) <> (Like.tupled, Like.unapply _)
  }
}

trait UsersComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class UsersTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def mobile = column[String]("mobile")
    def email = column[String]("email")
    def password = column[String]("password")
    def nickname = column[String]("nickname")
    def avatar = column[String]("avatar")
    def cover = column[String]("cover")
    def created = column[Long]("created")
    def updated = column[Long]("updated")
    def likes = column[Long]("likes")
    def refreshToken = column[String]("refresh_token")

    override def * = (id.?, mobile.?, email.?, password, nickname, avatar, cover, created, updated, likes, refreshToken.?) <> (User.tupled, User.unapply _)
  }
}

trait NotificationsComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class NotificationsTable(tag: Tag) extends Table[Notification](tag, "notify") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def `type` = column[String]("type")
    def userId = column[Long]("user_id")
    def fromUserId = column[Long]("from_user_id")
    def updated = column[Long]("updated")
    def tagName = column[String]("tag_name")
    def postId = column[Long]("post_id")

    override def * = (id.?, `type`, userId, fromUserId, updated, tagName.?, postId.?) <> (Notification.tupled, Notification.unapply _)
  }

}

trait SocialAccountsComponent {
  self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class SocialAccountsTable(tag: Tag) extends Table[SocialAccount](tag, "social") {
    def provider = column[String]("provider")
    def key = column[String]("key")
    def userId = column[Long]("user_id")

    override def * = (provider, key, userId) <> (SocialAccount.tupled, SocialAccount.unapply _)
  }
}