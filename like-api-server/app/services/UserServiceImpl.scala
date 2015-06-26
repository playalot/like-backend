package services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.likeorz.dao._
import com.likeorz.models._
import extensions.MobileProvider
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
    with UsersComponent with SocialAccountsComponent
    with FollowsComponent with BlocksComponent
    with MarksComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val users = TableQuery[UsersTable]
  private val follows = TableQuery[FollowsTable]
  private val blocks = TableQuery[BlocksTable]
  private val socials = TableQuery[SocialAccountsTable]

  override def findById(id: Long): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  override def findByMobileLegacy(mobilePhoneNumber: String): Future[Option[User]] = db.run(users.filter(_.mobile === mobilePhoneNumber).result.headOption)

  override def findByMobileAndZone(mobilePhoneNumber: String, zone: String): Future[Option[User]] = {
    val key = s"$zone $mobilePhoneNumber"
    db.run(socials.filter(u => u.provider === MobileProvider.ID && u.key === key).result.headOption).flatMap {
      case Some(social) => db.run(users.filter(_.id === social.userId).result.headOption)
      case None =>
        if (mobilePhoneNumber.startsWith("1") || mobilePhoneNumber.startsWith("666")) {
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
    db.run(socials += SocialAccount(providerId, providerKey, userId)).map(_ == 1)
  }

  override def unlinkAccount(userId: Long, providerId: String): Future[Boolean] = {
    db.run(socials.filter(x => x.userId === userId && x.provider === providerId).delete).map(_ == 1)
  }

  override def listLinkedAccounts(userId: Long): Future[Map[String, String]] = {
    findById(userId).flatMap {
      case Some(user) =>
        db.run(socials.filter(_.userId === userId).result).map { accounts =>
          if (user.mobile.isDefined && user.mobile.get.length > 0)
            accounts.map(social => (social.provider, social.key)).toMap + ("mobile" -> ("86 " + user.mobile.get))
          else
            accounts.map(social => (social.provider, social.key)).toMap
        }
      case None => Future.successful(Map())
    }
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

  override def getNickname(userId: Long): Future[String] = db.run(users.filter(_.id === userId).map(_.nickname).result.headOption).map(_.getOrElse(""))

  override def nicknameExists(nickname: String): Future[Boolean] = db.run(users.filter(_.nickname === nickname).result.headOption).map(_.isDefined)

  override def count(): Future[Int] = db.run(users.length.result)

  override def countFollowers(id: Long): Future[Int] = db.run(follows.filter(_.toId === id).length.result)

  override def getFollowers(userId: Long, page: Int): Future[Seq[User]] = {
    val query = (for {
      (follow, user) <- follows join users on (_.fromId === _.id)
      if follow.toId === userId
    } yield (follow, user)).sortBy(_._1.created.desc).drop(page * 20).take(20)
    db.run(query.map(_._2).result)
  }

  override def countFriends(id: Long): Future[Int] = db.run(follows.filter(_.fromId === id).length.result)

  override def getFriends(userId: Long, page: Int): Future[Seq[User]] = {
    val query = (for {
      (follow, user) <- follows join users on (_.toId === _.id)
      if follow.fromId === userId
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

  override def updateRefreshToken(id: Long, token: String): Future[Boolean] = {
    db.run(users.filter(_.id === id).map(x => (x.refreshToken, x.updated)).update((token, GenerateUtils.currentSeconds()))).map(_ == 1)
  }

  override def updateNickname(id: Long, nickname: String): Future[Boolean] = {
    db.run(users.filter(_.id === id).map(x => x.nickname).update(nickname)).map(_ == 1)
  }

  override def updateAvatar(id: Long, avatar: String): Future[Boolean] = {
    db.run(users.filter(_.id === id).map(x => x.avatar).update(avatar)).map(_ == 1)
  }

  override def updateCover(id: Long, cover: String): Future[Boolean] = {
    db.run(users.filter(_.id === id).map(x => x.cover).update(cover)).map(_ == 1)
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
            for {
              updateFollower <- db.run(updateQuery.update(true))
              insert <- db.run(follows += Follow(None, fromId, toId, both = true))
            } yield { 2 }
          case None =>
            for {
              insert <- db.run(follows += Follow(None, fromId, toId, both = false))
            } yield { 1 }
        }
    }
  }

  override def unFollow(fromId: Long, toId: Long): Future[Int] = {
    val updateQuery = for { f <- follows if f.fromId === toId && f.toId === fromId } yield f.both
    for {
      updateFollower <- db.run(updateQuery.update(false))
      remove <- db.run(follows.filter(f => f.fromId === fromId && f.toId === toId).delete)
    } yield remove
  }

  override def block(fromId: Long, toId: Long): Future[Int] = {
    db.run(blocks.filter(b => b.userId === fromId && b.blockedUserId === toId).result.headOption).flatMap {
      case Some(block) => Future.successful(0)
      case None        => db.run(blocks += Block(None, fromId, toId))
    }
  }

  override def unBlock(fromId: Long, toId: Long): Future[Int] = {
    db.run(blocks.filter(b => b.userId === fromId && b.blockedUserId === toId).delete)
  }
}
