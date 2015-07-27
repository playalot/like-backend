package services

import javax.inject.Inject

import com.likeorz.dao.UsersComponent
import com.likeorz.models.{ User, CachedUserInfo }
import com.likeorz.utils.KeyUtils
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile
import utils.RedisCacheClient

import scala.concurrent.Future

class UserServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends UserService
  with UsersComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def findById(id: Long): Future[Option[User]] = db.run(users.filter(_.id === id).result.headOption)

  override def getUserInfo(userId: Long): Future[CachedUserInfo] = {
    implicit def long2String(value: Long): String = value.toString

    val fields = RedisCacheClient.hmget(KeyUtils.user(userId), "nickname", "avatar", "cover", "likes")
    if (fields.contains(null)) {
      findById(userId).map {
        case Some(user) =>
          RedisCacheClient.hmset(KeyUtils.user(userId), Map[String, String](
            "nickname" -> user.nickname,
            "avatar" -> user.avatar,
            "cover" -> user.cover,
            "likes" -> user.likes))
          CachedUserInfo(user.nickname, user.avatar, user.cover, user.likes)
        case None =>
          CachedUserInfo("New Liker", "default_avatar", "default_cover", "0")
      }
    } else {
      Future.successful(CachedUserInfo(fields.head, fields(1), fields(2), fields(3)))
    }
  }

}
