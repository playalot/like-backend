package com.likeorz.services

import javax.inject.Inject

import com.likeorz.dao.{ SocialAccountsComponent, UsersComponent }
import com.likeorz.models.{ SocialAccount, User }
import com.likeorz.silhouettes.MobileProvider
import com.likeorz.utils.{ GlobalConstants, KeyUtils, RedisCacheClient, GenerateUtils }
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

class UserSettingService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
    with UsersComponent with SocialAccountsComponent {

  import driver.api._

  def linkAccount(userId: Long, providerId: String, providerKey: String): Future[Boolean] = {
    db.run(socials += SocialAccount(providerId, providerKey, userId)).map(_ == 1)
  }

  def unlinkAccount(userId: Long, providerId: String): Future[Boolean] = {
    db.run(socials.filter(x => x.userId === userId && x.provider === providerId).delete).map(_ == 1)
  }

  def updateMobile(userId: Long, mobilePhoneNumber: String, zone: Int): Future[Unit] = {
    val key = s"$zone $mobilePhoneNumber"
    db.run(socials.filter(u => u.provider === MobileProvider.ID && u.userId === userId).result.headOption).flatMap {
      case Some(social) =>
        db.run(socials.filter(u => u.provider === MobileProvider.ID && u.userId === userId).update(SocialAccount(MobileProvider.ID, key, userId))).map(_ => ())
      case None =>
        db.run(socials += SocialAccount(MobileProvider.ID, key, userId)).map(_ => ())
    }
    db.run(users.filter(_.id === userId).map(x => x.mobile).update(mobilePhoneNumber)).map(_ => ())
  }

  def update(id: Long, user: User): Future[User] = {
    val userToUpdate: User = user.copy(Some(id))
    db.run(users.filter(_.id === id).update(userToUpdate)).map(_ => userToUpdate)
  }

  def updateRefreshToken(id: Long, token: String): Future[Boolean] = {
    db.run(users.filter(_.id === id).map(x => (x.refreshToken, x.updated)).update((token, GenerateUtils.currentSeconds()))).map(_ == 1)
  }

  def updateNickname(id: Long, nickname: String): Future[Boolean] = {
    db.run(users.filter(_.id === id).map(x => x.nickname).update(nickname)).map { rs =>
      if (rs == 1) {
        RedisCacheClient.hset(KeyUtils.user(id), "nickname", nickname)
        true
      } else { false }
    }
  }

  def updateAvatar(id: Long, avatar: String): Future[Boolean] = {
    db.run(users.filter(_.id === id).map(x => x.avatar).update(avatar)).map { rs =>
      if (rs == 1) {
        RedisCacheClient.hset(KeyUtils.user(id), "avatar", avatar)
        true
      } else { false }
    }
  }

  def updateCover(id: Long, cover: String): Future[Boolean] = {
    db.run(users.filter(_.id === id).map(x => x.cover).update(cover)).map { rs =>
      if (rs == 1) {
        RedisCacheClient.hset(KeyUtils.user(id), "cover", cover)
        true
      } else { false }
    }
  }

  def unregister(id: Long): Future[Int] = {
    db.run(socials.filter(_.userId === id).delete).flatMap { _ =>
      db.run(users.filter(_.id === id).map(u => (u.nickname, u.avatar, u.cover, u.mobile, u.email))
        .update((GlobalConstants.DefaultNickname, GlobalConstants.DefaultAvatar, GlobalConstants.DefaultCover, null, null)))
    }
  }

}
