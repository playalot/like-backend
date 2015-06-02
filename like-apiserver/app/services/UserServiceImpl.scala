package services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import dao.{ NotificationsComponent, MarksComponent, FollowsComponent, UsersComponent }
import extensions.MobileProvider
import models.{ Notification, Follow, User }
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile
import utils.GenerateUtils
import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/22/15
 */
class UserServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends UserService
    with UsersComponent with FollowsComponent
    with MarksComponent with NotificationsComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val users = TableQuery[UsersTable]
  private val follows = TableQuery[FollowsTable]
  private val notifications = TableQuery[NotificationsTable]

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    if (loginInfo.providerID == MobileProvider.ID) {
      findByMobile(loginInfo.providerKey)
    } else {
      // TODO Add social auth
      Future.successful(None)
    }
  }

  override def findById(id: Long): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  override def findByMobile(mobilePhoneNumber: String): Future[Option[User]] = db.run(users.filter(_.mobile === mobilePhoneNumber).result.headOption)

  override def nicknameExists(nickname: String): Future[Boolean] = db.run(users.filter(_.nickname === nickname).result.headOption).map(_.isDefined)

  override def count(): Future[Int] = db.run(users.length.result)

  override def countFollowers(id: Long): Future[Int] = db.run(follows.filter(_.toId === id).result.map(_.length))

  override def countFriends(id: Long): Future[Int] = db.run(follows.filter(_.fromId === id).result.map(_.length))

  override def insert(user: User): Future[User] = {
    db.run(users returning users.map(_.id) += user).map(id => user.copy(id = Some(id)))
  }

  override def update(id: Long, user: User): Future[User] = {
    val userToUpdate: User = user.copy(Some(id))
    db.run(users.filter(_.id === id).update(userToUpdate)).map(_ => userToUpdate)
  }

  override def updateRefreshToken(id: Long, token: String): Future[Unit] = {
    db.run(users.filter(_.id === id).map(x => (x.refreshToken, x.updated)).update((token, GenerateUtils.currentSeconds))).map(_ => ())
  }

  override def updateNickname(id: Long, nickname: String): Future[Unit] = {
    db.run(users.filter(_.id === id).map(x => x.nickname).update(nickname)).map(_ => ())
  }

  override def updateAvatar(id: Long, avatar: String): Future[Unit] = {
    db.run(users.filter(_.id === id).map(x => x.avatar).update(avatar)).map(_ => ())
  }

  override def isFollowing(fromId: Long, toId: Long): Future[Int] = {
    for {
      fs <- db.run(follows.filter(f => f.fromId === fromId && f.toId === toId).result.headOption).map(_.isDefined)
      fd <- db.run(follows.filter(f => f.toId === fromId && f.fromId === toId).result.headOption).map(_.isDefined)
    } yield {
      if (!fs) 0
      else if (!fd) 1
      else 2
    }
  }

  override def follow(fromId: Long, toId: Long): Future[Int] = {
    db.run(follows.filter(f => f.fromId === fromId && f.toId === toId).result.headOption).flatMap {
      case Some(fs) => Future.successful(if (fs.both) 2 else 1)
      case None =>
        db.run(follows.filter(f => f.fromId === toId && f.toId === fromId).result.headOption).flatMap {
          case Some(fd) => {
            val updateQuery = for { f <- follows if f.fromId === toId && f.toId === fromId } yield f.both
            val notifyFollow = Notification(None, "FOLLOW", toId, fromId, System.currentTimeMillis / 1000, None, None)
            for {
              updateFollower <- db.run(updateQuery.update(true))
              insert <- db.run(follows += Follow(None, fromId, toId, true))
              notify <- db.run(notifications += notifyFollow)
            } yield { 2 }
          }
          case None =>
            val notifyFollow = Notification(None, "FOLLOW", toId, fromId, System.currentTimeMillis / 1000, None, None)
            for {
              insert <- db.run(follows += Follow(None, fromId, toId, false))
              notify <- db.run(notifications += notifyFollow)
            } yield { 1 }
        }
    }
  }

}
