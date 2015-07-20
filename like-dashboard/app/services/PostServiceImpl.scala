package services

import javax.inject.Inject

import com.likeorz.dao._
import com.likeorz.models.{ User, Post }
import org.nlpcn.commons.lang.jianfan.JianFan
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.MySQLDriver.api._
import slick.driver.JdbcProfile
import utils.RedisCacheClient
import com.likeorz.utils.KeyUtils

import scala.concurrent.Future
import scala.util.Random

class PostServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends PostService
    with PostsComponent with UsersComponent
    with TagsComponent with MarksComponent
    with LikesComponent with CommentsComponent
    with RecommendsComponent with FollowsComponent
    with ReportsComponent with DeletedPhotosComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  override def getPostById(postId: Long): Future[Option[(Post, User)]] = {
    val query = for {
      (post, user) <- posts join users on (_.userId === _.id) if post.id === postId
    } yield (post, user)
    db.run(query.result.headOption)
  }

  override def getPostsByUserId(userId: Long, page: Int, pageSize: Int): Future[Seq[(Post, Seq[(Long, String, Int)])]] = {
    db.run(posts.filter(_.userId === userId).sortBy(_.created.desc).drop(page * pageSize).take(pageSize).result).flatMap { posts =>
      val futures = posts.map { post =>
        val cachedMarks = RedisCacheClient.zRevRangeByScore("post_mark:" + post.id.get, offset = 0, limit = 20).map(v => (v._1.toLong, v._2.toInt)).toMap
        if (cachedMarks.nonEmpty) {
          val markIds = cachedMarks.keySet.mkString(", ")
          val query = sql"""select m.id, t.tag from mark m inner join tag t on m.tag_id = t.id  where m.id in (#$markIds)""".as[(Long, String)]
          db.run(query).map { list =>
            val markList = list.map(row => (row._1, row._2, cachedMarks.getOrElse(row._1, 0)))
            (post, markList)
          }
        } else {
          Future.successful((post, Seq()))
        }
      }
      Future.sequence(futures.toList)
    }
  }

  override def getPostsByIds(ids: Set[Long]): Future[Seq[(Post, User, Seq[(Long, String, Int)])]] = {
    if (ids.isEmpty) {
      Future.successful(Seq[(Post, User, Seq[(Long, String, Int)])]())
    } else {
      val query = for {
        (post, user) <- posts join users on (_.userId === _.id)
        if post.id inSet ids
      } yield (post, user)
      db.run(query.result).flatMap { posts =>
        val futures = posts.map { postAndUser =>
          val cachedMarks = RedisCacheClient.zRevRangeByScore("post_mark:" + postAndUser._1.id.get, offset = 0, limit = 20).map(v => (v._1.toLong, v._2.toInt)).toMap

          if (cachedMarks.nonEmpty) {
            val markIds = cachedMarks.keySet.mkString(", ")
            val query = sql"""select m.id, t.tag from mark m inner join tag t on m.tag_id = t.id  where m.id in (#$markIds)""".as[(Long, String)]
            db.run(query).map { list =>
              val marklist = list.map(row => (row._1, row._2, cachedMarks.getOrElse(row._1, 0)))
              (postAndUser._1, postAndUser._2, marklist)
            }
          } else {
            Future.successful((postAndUser._1, postAndUser._2, Seq()))
          }
        }
        Future.sequence(futures.toList)
      }
    }
  }

  override def searchByTag(page: Int = 0, pageSize: Int = 18, name: String = "%"): Future[Seq[(Post, User)]] = {
    val offset = pageSize * page

    val jian = JianFan.f2j(name)
    val fan = JianFan.j2f(name)

    val query = (for {
      ((post, mark), tag) <- posts join marks on (_.id === _.postId) join tags on (_._2.tagId === _.id)
      if (tag.tagName.toLowerCase like s"%${jian.toLowerCase}%") || (tag.tagName.toLowerCase like s"%${fan.toLowerCase}%")
    } yield post)
      .groupBy(_.id).map(_._1)
      .sortBy(_.desc)
      .drop(offset)
      .take(pageSize)
    db.run(query.result).flatMap { ids =>
      val q = (for {
        (post, user) <- posts join users on (_.userId === _.id) if post.id inSet ids
      } yield (post, user)).sortBy(_._1.likes.desc)
      db.run(q.result)
    }
  }

  override def getPersonalCategoryPosts(userId: Long): Seq[Long] = {
    try {
      RedisCacheClient.lrange(KeyUtils.userCategory(userId), 0, 20).zipWithIndex.map {
        case (num, i) =>
          if (num.toDouble.toInt > 0) {
            // get posts with least view
            val ids = RedisCacheClient.zrangebyscore(KeyUtils.category(i), 0, Int.MaxValue, 0, num.toDouble.toInt)
            // Increase view count
            ids.foreach(id => RedisCacheClient.zIncrBy(KeyUtils.category(i), 1, id))
            ids.map(_.toLong)
          } else {
            Set[Long]()
          }
      }.toSeq.flatMap(x => x)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        Seq()
    }
  }

  override def getRandomUsers(): Future[Seq[User]] = {
    db.run(users.result).map { users =>
      Random.shuffle(users).take(20)
    }
  }

}
