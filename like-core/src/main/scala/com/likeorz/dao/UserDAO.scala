package com.likeorz.dao

import com.likeorz.models._
import play.api.db.slick.HasDatabaseConfig
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

  protected val follows = TableQuery[FollowsTable]
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

  protected val users = TableQuery[UsersTable]
}

trait UserInfoComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class UserInfoTable(tag: Tag) extends Table[UserInfo](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def nickname = column[String]("nickname")
    def avatar = column[String]("avatar")
    def cover = column[String]("cover")
    def likes = column[Long]("likes")
    override def * = (id, nickname, avatar, cover, likes) <> (UserInfo.tupled, UserInfo.unapply _)
  }

  protected val userinfo = TableQuery[UserInfoTable]
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
    def markId = column[Long]("mark_id")
    override def * = (id.?, `type`, userId, fromUserId, updated, tagName.?, postId.?, markId.?) <> (Notification.tupled, Notification.unapply _)
  }

  protected val notifications = TableQuery[NotificationsTable]
}

trait SocialAccountsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class SocialAccountsTable(tag: Tag) extends Table[SocialAccount](tag, "social") {
    def provider = column[String]("provider")
    def key = column[String]("key")
    def userId = column[Long]("user_id")

    override def * = (provider, key, userId) <> (SocialAccount.tupled, SocialAccount.unapply _)
  }

  protected val socials = TableQuery[SocialAccountsTable]
}

trait BlocksComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class BlocksTable(tag: Tag) extends Table[Block](tag, "block") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def blockedUserId = column[Long]("blocked_user_id")
    def created = column[Long]("created")

    override def * = (id.?, userId, blockedUserId, created) <> (Block.tupled, Block.unapply _)
  }

  protected val blocks = TableQuery[BlocksTable]
}
