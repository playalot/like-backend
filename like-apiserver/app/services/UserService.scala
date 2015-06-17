package services

import com.mohiva.play.silhouette.api.LoginInfo
import models.{ SocialAccount, User }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/22/15
 */
trait UserService {

  def findById(id: Long): Future[Option[User]]

  def findByMobileLegacy(mobilePhoneNumber: String): Future[Option[User]]

  def findByMobileAndZone(mobilePhoneNumber: String, zone: Int): Future[Option[User]]

  def findBySocial(providerId: String, providerKey: String): Future[Option[SocialAccount]]

  def linkAccount(userId: Long, providerId: String, providerKey: String): Future[Boolean]

  def unlinkAccount(userId: Long, providerId: String): Future[Unit]

  def listLinkedAccounts(userId: Long): Future[Seq[SocialAccount]]

  def updateMobile(userId: Long, mobilePhoneNumber: String, zone: Int): Future[Unit]

  def count(): Future[Int]

  def countFollowers(id: Long): Future[Int]

  def getFollowers(userId: Long, page: Int): Future[Seq[User]]

  def countFriends(id: Long): Future[Int]

  def getFriends(userId: Long, page: Int): Future[Seq[User]]

  def insert(user: User): Future[User]

  def upsert(loginInfo: LoginInfo, user: User): Future[User]

  def update(id: Long, user: User): Future[User]

  def updateRefreshToken(id: Long, token: String): Future[Unit]

  def updateNickname(id: Long, nickname: String): Future[Unit]

  def updateAvatar(id: Long, avatar: String): Future[Unit]

  def updateCover(id: Long, cover: String): Future[Unit]

  def isFollowing(fromId: Long, toId: Long): Future[Int]

  def nicknameExists(nickname: String): Future[Boolean]

  def follow(userId: Long, fromId: Long): Future[Int]

}
