package services

import com.likeorz.models.{ CachedUserInfo, User }

import scala.concurrent.Future

trait UserService {

  def findById(id: Long): Future[Option[User]]

  def getUserInfo(userId: Long): Future[CachedUserInfo]

}
