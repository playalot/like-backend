package services

import javax.inject.Inject

import com.likeorz.models._
import com.likeorz.dao._
import com.likeorz.utils.KeyUtils
import org.joda.time.DateTime
import org.nlpcn.commons.lang.jianfan.JianFan
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import slick.driver.JdbcProfile
import utils.RedisCacheClient

import scala.concurrent.Future
import scala.util.Random

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class PostServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends PostService
    with PostsComponent with UsersComponent
    with TagsComponent with MarksComponent
    with LikesComponent with CommentsComponent
    with RecommendsComponent with FollowsComponent
    with ReportsComponent with DeletedPhotosComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def insert(post: Post): Future[Post] = {
    db.run(posts returning posts.map(_.id) += post).map { id =>
      RedisCacheClient.hincrBy(KeyUtils.user(post.userId), "posts", 1)
      post.copy(id = Some(id))
    }
  }

  override def countPostsForUser(userId: Long): Future[Long] = {
    RedisCacheClient.hget(KeyUtils.user(userId), "posts") match {
      case Some(number) => Future.successful(number.toLong)
      case None =>
        db.run(posts.filter(_.userId === userId).length.result).map { number =>
          RedisCacheClient.hset(KeyUtils.user(userId), "posts", number.toString)
          number
        }
    }
  }

  override def getPostsByUserId(userId: Long, page: Int, pageSize: Int): Future[Seq[(Post, Seq[(Long, String, Int)])]] = {
    db.run(posts.filter(_.userId === userId).sortBy(_.created.desc).drop(page * pageSize).take(pageSize).result).flatMap { posts =>
      val futures = posts.map { post =>
        val cachedMarks = RedisCacheClient.zrevrangebyscore("post_mark:" + post.id.get, offset = 0, limit = 20).map(v => (v._1.toLong, v._2.toInt)).toMap
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

  override def getPostsByIds(ids: Seq[Long]): Future[Seq[(Post, Seq[(Long, String, Int)])]] = {
    if (ids.isEmpty) {
      Future.successful(Seq[(Post, Seq[(Long, String, Int)])]())
    } else {
      db.run(posts.filter(_.id inSet ids).result).flatMap { posts =>
        val futures = posts.map { post =>
          val cachedMarks = RedisCacheClient.zrevrangebyscore("post_mark:" + post.id.get, offset = 0, limit = 50).map(v => (v._1.toLong, v._2.toInt)).toMap
          if (cachedMarks.nonEmpty) {
            val markIds = cachedMarks.keySet.mkString(", ")
            val query = sql"""select m.id, t.tag from mark m inner join tag t on m.tag_id = t.id  where m.id in (#$markIds)""".as[(Long, String)]
            db.run(query).map { list =>
              val marklist = list.map(row => (row._1, row._2, cachedMarks.getOrElse(row._1, 0)))
              (post, marklist)
            }
          } else {
            Future.successful((post, Seq()))
          }
        }
        Future.sequence(futures.toList)
      }
    }
  }

  override def searchByTag(page: Int = 0, pageSize: Int = 18, name: String = "%"): Future[Seq[(Post, User)]] = {
    val offset = pageSize * page

    val jian = JianFan.f2j(name).toLowerCase
    val fan = JianFan.j2f(name).toLowerCase

    val query = sql"""SELECT DISTINCT p.id FROM post p INNER JOIN mark m ON p.id=m.post_id INNER JOIN tag t ON m.tag_id=t.id WHERE t.tag like '%#${jian}%' OR t.tag like '%#${fan}%' order by p.created desc  limit $offset,$pageSize""".as[Long]
    db.run(query).flatMap { postIds =>
      val q = (for {
        (post, user) <- posts join users on (_.userId === _.id) if post.id inSet postIds
      } yield (post, user)).sortBy(_._1.created.desc)
      db.run(q.result)
    }
  }

  override def findHotPostForTag(name: String, page: Int = 0, pageSize: Int = 20): Future[Seq[(Post, User)]] = {
    val offset = pageSize * page
    val ts = DateTime.now().minusDays(15).getMillis / 1000
    val query = (for {
      ((post, mark), tag) <- posts join marks on (_.id === _.postId) join tags on (_._2.tagId === _.id)
      if tag.tagName.toLowerCase === name.toLowerCase && post.created > ts
    } yield post.id)
      .sortBy(_.desc)
      .drop(offset)
      .take(100)
    db.run(query.result).flatMap { pool =>
      val ids = Random.shuffle(pool).take(pageSize)
      val q = (for {
        (post, user) <- posts join users on (_.userId === _.id) if post.id inSet ids
      } yield (post, user)).sortBy(_._1.likes.desc)
      db.run(q.result)
    }
  }

  override def getTagPostImage(name: String): Future[Option[String]] = {
    val query = (for {
      ((post, mark), tag) <- posts join marks on (_.id === _.postId) join tags on (_._2.tagId === _.id)
      if tag.tagName.toLowerCase === name
    } yield post)
      .sortBy(_.likes.desc).map(_.content)
    db.run(query.result.headOption)
  }

  override def getPostById(postId: Long): Future[Option[Post]] = {
    db.run(posts.filter(_.id === postId).result.headOption)
  }

  override def getMarksForPost(postId: Long, page: Int = 0, userId: Option[Long] = None): Future[(Seq[(Long, String, Long, Long, String, String, Long)], Set[Long], Map[Long, Int], Seq[(Comment, User, Option[User])])] = {
    // Get top 10 marks for the post from cache
    val cachedMarks = RedisCacheClient.zrevrangebyscore("post_mark:" + postId, offset = page * 10, limit = 10).map(v => (v._1.toLong, v._2.toInt)).toMap
    Logger.debug("Cached marks: " + cachedMarks)

    // Query marks with tagname and user order by created desc
    // TODO: user from cache
    val markIds = cachedMarks.keySet.mkString(", ")
    val marksQuery = sql"""SELECT m.id, t.tag, m.created, m.user_id, u.nickname, u.avatar, u.likes FROM mark m INNER JOIN tag t ON m.tag_id=t.id INNER JOIN user u ON m.user_id=u.id WHERE m.id in (#$markIds)""".as[(Long, String, Long, Long, String, String, Long)]

    //    val mQuery = (for {
    //      ((mark, tag), user) <- marks join tags on (_.tagId === _.id) join users on (_._1.userId === _.id)
    //      if mark.id inSet cachedMarks.keySet
    //    } yield (mark.id, tag.tagName, mark.created, user))

    //    val likesQuery = for {
    //      like <- likes.filter(x => x.userId === userId.getOrElse(0L) && (x.markId inSet cachedMarks.keySet))
    //    } yield like.markId

    // Query all likes on the given set of marks
    val likesQuery = for {
      like <- likes.filter(x => x.userId === userId.getOrElse(0L) && (x.markId inSet cachedMarks.keySet))
    } yield like.markId

    // Query all comments on the give set of marks
    val commentsQuery = for {
      ((comment, user), reply) <- comments join users on (_.userId === _.id) joinLeft users on (_._1.replyId === _.id)
      if comment.markId inSet cachedMarks.keySet
    } yield (comment, user, reply)
    for {
      markList <- if (cachedMarks.keySet.nonEmpty) db.run(marksQuery) else Future.successful(Seq())
      likeList <- db.run(likesQuery.result)
      commentsOnMarks <- db.run(commentsQuery.result)
    } yield {
      //      (markList, cachedMarks, likeList, commentsOnMarks)
      (markList, likeList.toSet, cachedMarks, commentsOnMarks)
    }
  }

  override def deletePostById(postId: Long, userId: Long): Future[Unit] = {
    db.run(marks.filter(_.postId === postId).result).flatMap { markResults =>
      var total: Double = 0
      markResults.foreach { mark =>
        val likeNum = RedisCacheClient.zscore("post_mark:" + postId, mark.identify).getOrElse(0.0)
        // TODO remove
        RedisCacheClient.zincrby("tag_likes", -likeNum, mark.tagId.toString)
        RedisCacheClient.hincrBy(KeyUtils.user(mark.userId), "likes", -likeNum.toLong)
        total += likeNum
      }
      // TODO remove
      RedisCacheClient.zincrby("user_likes", -total, userId.toString)
      RedisCacheClient.del("post_mark:" + postId)
      RedisCacheClient.del("push:" + userId)
      RedisCacheClient.hincrBy(KeyUtils.user(userId), "posts", -1)

      val markIds = markResults.map(_.id.getOrElse(-1L))
      for {
        deleteLikes <- db.run(likes.filter(_.markId inSet markIds).delete)
        deleteComments <- db.run(comments.filter(_.markId inSet markIds).delete)
        deleteMarks <- db.run(marks.filter(_.postId === postId).delete)
        deletePost <- db.run(posts.filter(_.id === postId).delete)
      } yield {}
    }
  }

  override def addMark(postId: Long, authorId: Long, tagName: String, userId: Long): Future[Mark] = {
    db.run(tags.filter(_.tagName === tagName).result.headOption).flatMap {
      case Some(tag) => Future.successful(tag.id.get)
      case None      => db.run(tags returning tags.map(_.id) += Tag(None, tagName, userId))
    }.flatMap { tagId =>
      db.run(marks.filter(m => m.postId === postId && m.tagId === tagId).result.headOption).flatMap {
        case Some(mark) =>
          // If other people create an existed mark, it equals user like the mark
          // Note this is actually not executed since front-end disallow create same mark
          if (mark.userId != userId) {
            for {
              l <- db.run(likes += Like(mark.id.get, userId))
              //n <- db.run(notifications += Notification(None, "LIKE", mark.userId, userId, System.currentTimeMillis / 1000, Some(tagName), Some(postId)))
            } yield {
              // Increase post author likes count
              RedisCacheClient.hincrBy(KeyUtils.user(authorId), "likes", 1)
              // Increase mark author likes count
              if (mark.userId != authorId) RedisCacheClient.hincrBy(KeyUtils.user(mark.userId), "likes", 1)
              // Increase post mark likes count
              RedisCacheClient.zincrby("post_mark:" + postId, 1, mark.identify)
              mark
            }
          } else {
            Future.successful(mark)
          }
        case None =>
          val newMark = Mark(None, postId, tagId, Some(tagName), userId)
          for {
            mark <- db.run(marks returning marks.map(_.id) += newMark).map(id => newMark.copy(id = Some(id)))
            l <- db.run(likes += Like(mark.id.get, userId))
          } yield {
            // Increase post author likes count
            RedisCacheClient.hincrBy(KeyUtils.user(authorId), "likes", 1)
            // Increase mark author likes count
            if (userId != authorId) RedisCacheClient.hincrBy(KeyUtils.user(userId), "likes", 1)
            // Increase post mark likes count
            RedisCacheClient.zincrby("post_mark:" + postId, 1, mark.identify)
            mark
          }
      }
    }
  }

  override def report(report: Report): Future[Report] = {
    db.run(reports returning reports.map(_.id) += report).map(id => report.copy(id = Some(id)))
  }

  override def getRecommendedPosts(pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    if (timestamp.isDefined) {
      val query = (for {
        (recommend, post) <- recommends join posts on (_.postId === _.id) if post.created < timestamp.get
      } yield post.id).sortBy(x => x.desc).take(pageSize)
      db.run(query.result)
    } else {
      val query = (for {
        (recommend, post) <- recommends join posts on (_.postId === _.id)
      } yield post.id).sortBy(x => x.desc).take(pageSize)
      db.run(query.result)
    }
  }

  override def getFollowingPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    db.run(follows.filter(_.fromId === userId).map(_.toId).result).flatMap { userIds =>
      if (timestamp.isDefined) {
        db.run(posts.filter(p => p.userId.inSet(userIds.+:(userId)) && p.created < timestamp.get).sortBy(_.created.desc).map(_.id).take(pageSize).result)
      } else {
        db.run(posts.filter(_.userId.inSet(userIds.+:(userId))).sortBy(_.created.desc).map(_.id).take(pageSize).result)
      }
    }
  }

  override def getTaggedPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    val tagIdsQuery = sql"""SELECT DISTINCT tag_id FROM mark WHERE user_id=$userId order by created desc limit 100""".as[Long]
    db.run(tagIdsQuery).flatMap { tagIds =>
      if (tagIds.nonEmpty) {
        val tIds = tagIds.mkString(", ")
        val query = if (timestamp.isDefined) {
          val ts = timestamp.get
          sql"""SELECT DISTINCT p.id FROM post p INNER JOIN mark m ON p.id=m.post_id WHERE p.created < $ts AND m.tag_id IN (#${tIds}) order by p.created desc limit $pageSize""".as[Long]
        } else {
          sql"""SELECT DISTINCT p.id FROM post p INNER JOIN mark m ON p.id=m.post_id WHERE m.tag_id IN (#${tIds}) order by p.created desc limit $pageSize""".as[Long]
        }
        db.run(query)
      } else {
        Future.successful(Seq())
      }
    }
  }

  override def getRecentPosts(pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    if (timestamp.isDefined) {
      db.run(posts.filter(_.created < timestamp.get).sortBy(_.created.desc).take(pageSize).map(_.id).result)
    } else {
      db.run(posts.sortBy(_.created.desc).take(pageSize).map(_.id).result)
    }
  }

  override def recordDelete(photo: String): Future[Unit] = {
    db.run(deletes += DeletedPhoto(None, photo)).map(_ => ())
  }

  override def getPersonalizedPostsForUser(userId: Long, ratio: Double): Seq[Long] = {
    try {
      RedisCacheClient.lrange(KeyUtils.userCategory(userId), 0, 20).zipWithIndex.map {
        case (num, i) =>
          val n = (num.toDouble * ratio).toInt
          if (n > 0) {
            // get posts with least view
            val ids = RedisCacheClient.zrangebyscore(KeyUtils.category(i), 0, Int.MaxValue, 0, n)
            // Increase view count
            ids.foreach(id => RedisCacheClient.zincrby(KeyUtils.category(i), 1, id))
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

  override def get30DayHotUsers(num: Int): Future[Seq[User]] = {
    val cachedUsers = RedisCacheClient.srandmember(KeyUtils.hotUsers, 15)
    if (cachedUsers.isEmpty) {
      val ts30ago = DateTime.now().minusDays(30).getMillis / 1000
      val query = posts.filter(_.created > ts30ago)
        .groupBy(_.userId)
        .map(x => (x._1, x._2.size))
        .sortBy(_._2.desc)
        .take(100)
      db.run(query.result).flatMap { pool =>
        RedisCacheClient.sadd(KeyUtils.hotUsers, pool.map(_._1.toString))
        val userIds = Random.shuffle(pool).take(num).map(_._1)
        db.run(users.filter(u => (u.id inSet userIds) && u.avatar =!= "default_avatar.jpg").result)
      }
    } else {
      db.run(users.filter(u => (u.id inSet cachedUsers.map(_.toLong)) && u.avatar =!= "default_avatar.jpg").result)
    }
  }

  override def get7DayHotUsers(num: Int): Future[Seq[User]] = {
    val ts7ago = DateTime.now().minusDays(7).getMillis / 1000
    val query = (for {
      (post, user) <- posts join users on (_.userId === _.id)
      if user.created > ts7ago && user.avatar =!= "default_avatar.jpg"
    } yield user).groupBy(_.id).map(x => (x._1, x._2.size))
      .sortBy(_._2.desc)
      .take(30)

    db.run(query.result).flatMap { pool =>
      println(pool)
      val userIds = Random.shuffle(pool).take(num).map(_._1)
      println(userIds)
      db.run(users.filter(_.id inSet userIds).result)
    }
  }

}
