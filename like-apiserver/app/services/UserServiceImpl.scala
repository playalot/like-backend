package services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import dao._
import extensions.MobileProvider
import models.{ SocialAccount, Notification, Follow, User }
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
    with SocialAccountsComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val users = TableQuery[UsersTable]
  private val follows = TableQuery[FollowsTable]
  private val notifications = TableQuery[NotificationsTable]
  private val socials = TableQuery[SocialAccountsTable]

  override def findById(id: Long): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  override def findByMobileLegacy(mobilePhoneNumber: String): Future[Option[User]] = db.run(users.filter(_.mobile === mobilePhoneNumber).result.headOption)

  override def findByMobileAndZone(mobilePhoneNumber: String, zone: Int): Future[Option[User]] = {
    val key = s"$zone $mobilePhoneNumber"
    db.run(socials.filter(u => u.provider === MobileProvider.ID && u.key === key).result.headOption).flatMap {
      case Some(social) => db.run(users.filter(_.id === social.userId).result.headOption)
      case None =>
        if (mobilePhoneNumber.startsWith("1")) {
          db.run(users.filter(_.mobile === mobilePhoneNumber).result.headOption)
        } else {
          Future.successful(None)
        }
    }
  }

  override def findBySocial(providerId: String, providerKey: String): Future[Option[SocialAccount]] = {
    db.run(socials.filter(x => x.provider === providerId && x.key === providerKey).result.headOption)
  }

  override def linkAccount(userId: Long, providerId: String, providerKey: String): Future[Boolean] = {
    db.run(socials += SocialAccount(providerId, providerKey, userId)).map(x => x == 1)
  }

  override def unlinkAccount(userId: Long, providerId: String): Future[Unit] = {
    db.run(socials.filter(x => x.userId === userId && x.provider === providerId).delete).map(_ => ())
  }

  override def listLinkedAccounts(userId: Long): Future[Seq[SocialAccount]] = {
    db.run(socials.filter(_.userId === userId).result)
  }

  override def updateMobile(userId: Long, mobilePhoneNumber: String, zone: Int): Future[Unit] = {
    val key = s"$zone $mobilePhoneNumber"
    db.run(socials.filter(u => u.provider === MobileProvider.ID && u.userId === userId).result.headOption).flatMap {
      case Some(social) =>
        db.run(socials.filter(u => u.provider === MobileProvider.ID && u.userId === userId).update(SocialAccount(MobileProvider.ID, key, userId))).map(_ => ())
      case None =>
        db.run(socials += SocialAccount(MobileProvider.ID, key, userId)).map(_ => ())
    }
    db.run(users.filter(_.id === userId).map(x => x.mobile).update(mobilePhoneNumber)).map(_ => ())
  }

  override def nicknameExists(nickname: String): Future[Boolean] = db.run(users.filter(_.nickname === nickname).result.headOption).map(_.isDefined)

  override def count(): Future[Int] = db.run(users.length.result)

  override def countFollowers(id: Long): Future[Int] = db.run(follows.filter(_.toId === id).length.result)

  override def getFollowers(userId: Long, page: Int): Future[Seq[User]] = {
    val query = (for {
      (follow, user) <- follows join users on (_.fromId === _.id)
      if (follow.toId === userId)
    } yield (follow, user)).sortBy(_._1.created.desc).drop(page * 20).take(20)
    db.run(query.map(_._2).result)
  }

  override def countFriends(id: Long): Future[Int] = db.run(follows.filter(_.fromId === id).length.result)

  override def getFriends(userId: Long, page: Int): Future[Seq[User]] = {
    val query = (for {
      (follow, user) <- follows join users on (_.toId === _.id)
      if (follow.fromId === userId)
    } yield (follow, user)).sortBy(_._1.created.desc).drop(page * 20).take(20)
    db.run(query.map(_._2).result)
  }

  override def insert(user: User): Future[User] = {
    db.run(users returning users.map(_.id) += user).map(id => user.copy(id = Some(id)))
  }

  override def upsert(loginInfo: LoginInfo, user: User): Future[User] = {
    db.run(socials.filter(u => u.provider === loginInfo.providerID && u.key === loginInfo.providerKey).result.headOption).flatMap {
      case Some(social) =>
        for {
          unit <- db.run(users.filter(_.id === social.userId).map(x => (x.refreshToken, x.updated)).update((user.refreshToken.get, GenerateUtils.currentSeconds()))).map(_ => ())
          user <- db.run(users.filter(_.id === social.userId).result.head)
        } yield { user }
      case None =>
        for {
          u <- db.run(users returning users.map(_.id) += user).map(id => user.copy(id = Some(id)))
          s <- db.run(socials += SocialAccount(loginInfo.providerID, loginInfo.providerKey, u.id.get))
        } yield { u }
    }
  }

  override def update(id: Long, user: User): Future[User] = {
    val userToUpdate: User = user.copy(Some(id))
    db.run(users.filter(_.id === id).update(userToUpdate)).map(_ => userToUpdate)
  }

  override def updateRefreshToken(id: Long, token: String): Future[Unit] = {
    db.run(users.filter(_.id === id).map(x => (x.refreshToken, x.updated)).update((token, GenerateUtils.currentSeconds()))).map(_ => ())
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
          case Some(fd) =>
            val updateQuery = for { f <- follows if f.fromId === toId && f.toId === fromId } yield f.both
            val notifyFollow = Notification(None, "FOLLOW", toId, fromId, System.currentTimeMillis / 1000, None, None)
            for {
              updateFollower <- db.run(updateQuery.update(true))
              insert <- db.run(follows += Follow(None, fromId, toId, true))
              notify <- db.run(notifications += notifyFollow)
            } yield { 2 }
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
