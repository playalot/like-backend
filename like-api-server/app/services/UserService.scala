package services

import com.mohiva.play.silhouette.api.LoginInfo
import com.likeorz.models._

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/22/15
 */
trait UserService {

  def findById(id: Long): Future[Option[User]]

  def findByMobileLegacy(mobilePhoneNumber: String): Future[Option[User]]

  def findByMobileAndZone(mobilePhoneNumber: String, zone: String): Future[Option[User]]

  def findBySocial(providerId: String, providerKey: String): Future[Option[SocialAccount]]

  def linkAccount(userId: Long, providerId: String, providerKey: String): Future[Boolean]

  def unlinkAccount(userId: Long, providerId: String): Future[Boolean]

  def listLinkedAccounts(userId: Long): Future[Map[String, String]]

  def updateMobile(userId: Long, mobilePhoneNumber: String, zone: Int): Future[Unit]

  def countFollowers(id: Long): Future[Long]

  def getFollowers(userId: Long, page: Int): Future[Seq[User]]

  def countFollowings(id: Long): Future[Long]

  def getFollowings(userId: Long, page: Int): Future[Seq[User]]

  def insert(user: User): Future[User]

  def upsert(loginInfo: LoginInfo, user: User): Future[User]

  def update(id: Long, user: User): Future[User]

  def updateRefreshToken(id: Long, token: String): Future[Boolean]

  def updateNickname(id: Long, nickname: String): Future[Boolean]

  def updateAvatar(id: Long, avatar: String): Future[Boolean]

  def updateCover(id: Long, cover: String): Future[Boolean]

  def isFollowing(fromId: Long, toId: Long): Future[Int]

  def getNickname(userId: Long): Future[String]

  def nicknameExists(nickname: String): Future[Boolean]

  def follow(fromId: Long, toId: Long): Future[Int]

  def unFollow(fromId: Long, toId: Long): Future[Int]

  def block(fromId: Long, toId: Long): Future[Int]

  def unBlock(fromId: Long, toId: Long): Future[Int]

  def getUserInfo(userId: Long): Future[CachedUserInfo]

}
