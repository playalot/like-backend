package com.likeorz.services

import com.likeorz.models._
import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.Future

trait UserService {

  def findById(id: Long): Future[Option[User]]

  def findByMobileLegacy(mobilePhoneNumber: String): Future[Option[User]]

  def findByMobileAndZone(mobilePhoneNumber: String, zone: String): Future[Option[User]]

  def findBySocial(providerId: String, providerKey: String): Future[Option[SocialAccount]]

  def listLinkedAccounts(userId: Long): Future[Map[String, String]]

  def insert(user: User): Future[User]

  def upsert(loginInfo: LoginInfo, user: User): Future[User]

  def syncDBLikesFromCache(userId: Long): Future[Unit]

  def isFollowing(fromId: Long, toId: Long): Future[Int]

  def isBlocking(fromId: Long, toId: Long): Future[Boolean]

  def getNickname(userId: Long): Future[String]

  def searchByName(name: String): Future[Seq[User]]

  def nicknameExists(nickname: String): Future[Boolean]

  def countFollowers(id: Long): Future[Long]

  def countFollowing(id: Long): Future[Long]

  def refreshUserCount(id: Long): Future[Unit]

  def block(fromId: Long, toId: Long): Future[Int]

  def unBlock(fromId: Long, toId: Long): Future[Int]

  def getUserInfo(userId: Long): Future[CachedUserInfo]

  def getUserInfoFromCache(userId: Long): CachedUserInfo

  def filterUsersByNameAndMobile(pageSize: Int, page: Int, filter: String): Future[Seq[User]]

  def getActiveUserIds(timerange: Long): Seq[(Long, Long)]

  def getBannedUserIds: Set[Long]

  def getBlockedUserIdsForUser(userId: Long): Future[Seq[Long]]
}
