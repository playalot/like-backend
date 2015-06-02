package services

import com.mohiva.play.silhouette.api.services.IdentityService
import models.User

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/22/15
 */
trait UserService extends IdentityService[User] {

  def findById(id: Long): Future[Option[User]]

  def findByMobile(mobilePhoneNumber: String): Future[Option[User]]

  def count(): Future[Int]

  def countFollowers(id: Long): Future[Int]

  def countFriends(id: Long): Future[Int]

  def insert(user: User): Future[User]

  def update(id: Long, user: User): Future[User]

  def updateRefreshToken(id: Long, token: String): Future[Unit]

  def updateNickname(id: Long, nickname: String): Future[Unit]

  def updateAvatar(id: Long, avatar: String): Future[Unit]

  def isFollowing(fromId: Long, toId: Long): Future[Int]

  def nicknameExists(nickname: String): Future[Boolean]

  def follow(userId: Long, fromId: Long): Future[Int]

}
