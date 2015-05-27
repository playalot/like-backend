package dao

import javax.inject.Inject

import models.{ Mark, Follow, User }
import play.api.db.slick.{ HasDatabaseConfig, DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.driver.JdbcProfile
import utils.GenerateUtils

import scala.concurrent.Future

trait FollowsComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  class FollowsTable(tag: Tag) extends Table[Follow](tag, "follow") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def fromId = column[Long]("from_id")
    def toId = column[Long]("to_id")
    def both = column[Boolean]("both")
    def created = column[Long]("created")

    override def * = (id, fromId, toId, both, created) <> (Follow.tupled, Follow.unapply _)
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

    override def * = (id, postId, tagId, userId, created, updated, likes) <> (Mark.tupled, Mark.unapply _)
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

    override def * = (id.?, mobile, email.?, password, nickname, avatar, cover, created, updated, likes, refreshToken.?) <> (User.tupled, User.unapply _)
  }
}

/*
trait UserDAO {
  def findById(id: Long): Future[Option[User]]
  def findByMobile(mobilePhoneNumber: String): Future[Option[User]]
  def nicknameExists(nickname: String): Future[Boolean]
  def count(): Future[Int]
  def countFollowers(id: Long): Future[Int]
  def countFriends(id: Long): Future[Int]
  def insert(user: User): Future[User]
  def update(id: Long, user: User): Future[User]
  def updateRefreshToken(id: Long, token: String): Future[Unit]
  def updateNickname(id: Long, nickname: String): Future[Unit]
  def updateAvatar(id: Long, avatar: String): Future[Unit]
  def isFollowing(fromId: Long, toId: Long): Future[Int]
}

class UserDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
    extends UserDAO
    with UsersComponent with FollowsComponent with MarksComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val users = TableQuery[UsersTable]
  private val follows = TableQuery[FollowsTable]

  def findById(id: Long): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  def findByMobile(mobilePhoneNumber: String): Future[Option[User]] = db.run(users.filter(_.mobile === mobilePhoneNumber).result.headOption)

  def nicknameExists(nickname: String): Future[Boolean] = db.run(users.filter(_.nickname === nickname).result.headOption).map(_.isDefined)

  def count(): Future[Int] = db.run(users.length.result)

  def countFollowers(id: Long): Future[Int] = db.run(follows.filter(_.toId === id).result.map(_.length))

  def countFriends(id: Long): Future[Int] = db.run(follows.filter(_.fromId === id).result.map(_.length))

  def insert(user: User): Future[User] = {
    db.run(users returning users.map(_.id) += user).map(id => user.copy(id = Some(id)))
  }

  def update(id: Long, user: User): Future[User] = {
    val userToUpdate: User = user.copy(Some(id))
    db.run(users.filter(_.id === id).update(userToUpdate)).map(_ => userToUpdate)
  }

  def updateRefreshToken(id: Long, token: String): Future[Unit] = {
    db.run(users.filter(_.id === id).map(x => (x.refreshToken, x.updated)).update((token, GenerateUtils.currentSeconds))).map(_ => ())
  }

  def updateNickname(id: Long, nickname: String): Future[Unit] = {
    db.run(users.filter(_.id === id).map(x => x.nickname).update(nickname)).map(_ => ())
  }

  def updateAvatar(id: Long, avatar: String): Future[Unit] = {
    db.run(users.filter(_.id === id).map(x => x.avatar).update(avatar)).map(_ => ())
  }

  def isFollowing(fromId: Long, toId: Long): Future[Int] = {
    for {
      fs <- db.run(follows.filter(f => f.fromId === fromId && f.toId === toId).result.headOption).map(_.isDefined)
      fd <- db.run(follows.filter(f => f.toId === fromId && f.fromId === toId).result.headOption).map(_.isDefined)
    } yield {
      if (!fs) 0
      else if (!fd) 1
      else 2
    }
  }

}*/ 