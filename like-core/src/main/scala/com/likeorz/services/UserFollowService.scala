package com.likeorz.services

import javax.inject.Inject

import com.likeorz.dao.{ UserInfoComponent, FollowsComponent }
import com.likeorz.models.{ Follow, UserInfo }
import com.likeorz.utils.{ KeyUtils, RedisCacheClient }
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import slick.driver.JdbcProfile

class UserFollowService @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile]
    with FollowsComponent with UserInfoComponent {

  import driver.api._

  def getFollowers(userId: Long, page: Int): Future[Seq[(UserInfo, Boolean)]] = {
    val query = (for {
      follow <- follows if follow.toId === userId
      user <- userinfo if follow.fromId === user.id
    } yield (follow, user)).sortBy(_._1.created.desc).drop(page * 20).take(20)
    db.run(query.map(rs => (rs._2, rs._1.both)).result)
  }

  def getFollowings(userId: Long, page: Int): Future[Seq[UserInfo]] = {
    val query = (for {
      follow <- follows if follow.fromId === userId
      user <- userinfo if follow.toId === user.id
    } yield (follow, user)).sortBy(_._1.created.desc).drop(page * 20).take(20)
    db.run(query.map(_._2).result)
  }

  def follow(fromId: Long, toId: Long): Future[Int] = {
    db.run(follows.filter(f => f.fromId === fromId && f.toId === toId).result.headOption).flatMap {
      case Some(fs) => Future.successful(if (fs.both) 2 else 1)
      case None =>
        // Update db
        db.run(follows.filter(f => f.fromId === toId && f.toId === fromId).result.headOption).flatMap {
          case Some(fd) =>
            val updateQuery = for { f <- follows if f.fromId === toId && f.toId === fromId } yield f.both
            for {
              updateFollower <- db.run(updateQuery.update(true))
              insert <- db.run(follows += Follow(None, fromId, toId, both = true))
            } yield {
              // Update cache
              RedisCacheClient.hincrBy(KeyUtils.user(fromId), "followings", 1)
              RedisCacheClient.hincrBy(KeyUtils.user(toId), "followers", 1)
              2
            }
          case None =>
            for {
              insert <- db.run(follows += Follow(None, fromId, toId, both = false))
            } yield {
              // Update cache
              RedisCacheClient.hincrBy(KeyUtils.user(fromId), "followings", 1)
              RedisCacheClient.hincrBy(KeyUtils.user(toId), "followers", 1)
              1
            }
        }
    }
  }

  def unFollow(fromId: Long, toId: Long): Future[Int] = {
    val updateQuery = for { f <- follows if f.fromId === toId && f.toId === fromId } yield f.both
    for {
      updateFollower <- db.run(updateQuery.update(false))
      remove <- db.run(follows.filter(f => f.fromId === fromId && f.toId === toId).delete)
    } yield {
      RedisCacheClient.hincrBy(KeyUtils.user(fromId), "followings", -1)
      RedisCacheClient.hincrBy(KeyUtils.user(toId), "followers", -1)
      remove
    }
  }

}
