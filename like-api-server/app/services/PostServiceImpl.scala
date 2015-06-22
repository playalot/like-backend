package services

import javax.inject.Inject

import dao._
import models._
import org.nlpcn.commons.lang.jianfan.JianFan
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import slick.driver.JdbcProfile
import utils.RedisCacheClient

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class PostServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends PostService
    with PostsComponent with UsersComponent
    with TagsComponent with MarksComponent
    with LikesComponent with CommentsComponent
    with NotificationsComponent with ReportsComponent
    with RecommendsComponent with FollowsComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val posts = TableQuery[PostsTable]
  private val tags = TableQuery[TagsTable]
  private val marks = TableQuery[MarksTable]
  private val likes = TableQuery[LikesTable]
  private val users = TableQuery[UsersTable]
  private val comments = TableQuery[CommentsTable]
  private val notifications = TableQuery[NotificationsTable]
  private val reports = TableQuery[ReportsTable]
  private val recommends = TableQuery[RecommendsTable]
  private val follows = TableQuery[FollowsTable]

  override def insert(post: Post): Future[Post] = {
    db.run(posts returning posts.map(_.id) += post).map(id => post.copy(id = Some(id)))
  }

  override def countByUserId(userId: Long): Future[Long] = {
    db.run(posts.filter(_.userId === userId).result.map(_.length))
  }

  override def getPostsByUserId(userId: Long, page: Int, pageSize: Int): Future[Seq[(Post, Seq[(Long, String, Int)])]] = {
    db.run(posts.filter(_.userId === userId).sortBy(_.created.desc).drop(page * pageSize).take(pageSize).result).flatMap { posts =>
      val futures = posts.map { post =>
        val cachedMarks = RedisCacheClient.zRevRangeByScore("post_mark:" + post.id.get, offset = 0, limit = 20).map(v => (v._1.toLong, v._2.toInt)).toMap
        val query = for {
          (mark, tag) <- marks join tags on (_.tagId === _.id)
          if mark.id inSet cachedMarks.keySet
        } yield (mark.id, tag.tagName)

        db.run(query.result).map { list =>
          val marklist = list.map(row => (row._1, row._2, cachedMarks.getOrElse(row._1, 0)))
          (post, marklist)
        }
      }
      Future.sequence(futures.toList)
    }
  }

  override def getPostsByIds(ids: Set[Long]): Future[Seq[(Post, Seq[(Long, String, Int)])]] = {
    db.run(posts.filter(_.id inSet ids).result).flatMap { posts =>
      val futures = posts.map { post =>
        val cachedMarks = RedisCacheClient.zRevRangeByScore("post_mark:" + post.id.get, offset = 0, limit = 20).map(v => (v._1.toLong, v._2.toInt)).toMap
        val query = for {
          (mark, tag) <- marks join tags on (_.tagId === _.id)
          if mark.id inSet cachedMarks.keySet
        } yield (mark.id, tag.tagName)

        db.run(query.result).map { list =>
          val marklist = list.map(row => (row._1, row._2, cachedMarks.getOrElse(row._1, 0)))
          (post, marklist)
        }
      }
      Future.sequence(futures.toList)
    }
  }

  override def searchByTag(page: Int = 0, pageSize: Int = 18, name: String = "%"): Future[Seq[(Post, User)]] = {
    val offset = pageSize * page

    val jian = JianFan.f2J(name)
    val fan = JianFan.j2F(name)

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

  override def getPostById(postId: Long): Future[Option[(Post, User)]] = {
    val query = for {
      (post, user) <- posts join users on (_.userId === _.id) if post.id === postId
    } yield (post, user)
    db.run(query.result.headOption)
  }

  override def getMarksForPost(postId: Long, page: Int = 0, userId: Option[Long] = None): Future[(Seq[(Long, String, Long, User)], Set[Long], Map[Long, Int], Seq[(Comment, User, Option[User])])] = {
    val cachedMarks = RedisCacheClient.zRevRangeByScore("post_mark:" + postId, offset = page * 10, limit = 10).map(v => (v._1.toLong, v._2.toInt)).toMap
    Logger.debug("Cached marks: " + cachedMarks)

    val marksQuery = (for {
      ((mark, tag), user) <- marks join tags on (_.tagId === _.id) join users on (_._1.userId === _.id)
      if (mark.postId === postId) && (mark.id inSet cachedMarks.keySet)
    } yield (mark.id, tag.tagName, mark.created, user)).sortBy(_._3.desc)

    val likesQuery = for {
      like <- likes.filter(x => x.userId === userId.getOrElse(0L) && (x.markId inSet cachedMarks.keySet))
    } yield like.markId

    val commentsQuery = for {
      ((comment, user), reply) <- comments join users on (_.userId === _.id) joinLeft users on (_._1.replyId === _.id)
      if comment.markId inSet cachedMarks.keySet
    } yield (comment, user, reply)

    for {
      markList <- db.run(marksQuery.result)
      likeList <- db.run(likesQuery.result)
      commentsOnMarks <- db.run(commentsQuery.result)
    } yield {
      (markList, likeList.toSet, cachedMarks, commentsOnMarks)
    }
  }

  override def deletePostById(postId: Long, userId: Long): Future[Unit] = {
    val cachedMarks = RedisCacheClient.zRevRangeByScore("post_mark:" + postId, offset = 0, limit = 1000).map(v => (v._1.toLong, v._2.toInt)).toMap
    Logger.debug("Cached marks: " + cachedMarks)

    db.run(marks.filter(_.postId === postId).result).flatMap { markResults =>
      var total: Double = 0
      markResults.foreach { mark =>
        val likeNum = RedisCacheClient.zScore("post_mark:" + postId, mark.identify).getOrElse(mark.likes.toDouble)
        RedisCacheClient.zIncrBy("tag_likes", -likeNum, mark.tagId.toString)
        total += likeNum
      }
      RedisCacheClient.zIncrBy("user_likes", -total, userId.toString)
      RedisCacheClient.del("post_mark:" + postId)
      RedisCacheClient.del("push:" + userId)

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
          if (mark.userId != userId) {
            db.run(likes += Like(mark.id.get, userId))
            RedisCacheClient.zIncrBy("post_mark:" + postId, 1, mark.identify)
            val notifyMarkUser = Notification(None, "LIKE", mark.userId, userId, System.currentTimeMillis / 1000, Some(tagName), Some(postId))
            db.run(notifications += notifyMarkUser)
          }
          Future.successful(mark)
        case None =>
          val newMark = Mark(None, postId, tagId, userId)
          db.run(marks returning marks.map(_.id) += newMark).map(id => newMark.copy(id = Some(id))).map { mark =>
            RedisCacheClient.zIncrBy("post_mark:" + postId, 1, mark.identify)
            db.run(likes += Like(mark.id.get, userId))
            if (authorId != userId) {
              val notifyMarkUser = Notification(None, "MARK", authorId, userId, System.currentTimeMillis / 1000, Some(tagName), Some(postId))
              db.run(notifications += notifyMarkUser)
            }
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
      db.run(recommends.filter(_.created < timestamp.get).sortBy(_.created.desc).take(pageSize).map(_.postId).result)
    } else {
      db.run(recommends.sortBy(_.created.desc).take(pageSize).map(_.postId).result)
    }
  }

  override def getFollowingPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    db.run(follows.filter(_.fromId === userId).take(10000).map(_.toId).result).flatMap { userIds =>
      if (timestamp.isDefined) {
        db.run(posts.filter(p => p.userId.inSet(userIds.+:(userId)) && p.created < timestamp.get).sortBy(_.created.desc).take(pageSize).map(_.id).result)
      } else {
        db.run(posts.filter(_.userId.inSet(userIds.+:(userId))).sortBy(_.created.desc).take(pageSize).map(_.id).result)
      }
    }
  }

  override def getTaggedPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    db.run(marks.filter(_.userId === userId).map(_.tagId).groupBy(x => x).map(_._1).take(10000).result).flatMap { tagIds =>
      if (timestamp.isDefined) {
        val query = (for {
          (mark, post) <- marks join posts on (_.postId === _.id)
          if mark.userId =!= userId && mark.tagId.inSet(tagIds) && mark.created < timestamp.get
        } yield (post)).groupBy(_.id).map(_._1).sortBy(_.desc).take(pageSize)
        db.run(query.result)
      } else {
        val query = (for {
          (mark, post) <- marks join posts on (_.postId === _.id)
          if mark.userId =!= userId && mark.tagId.inSet(tagIds)
        } yield (post)).groupBy(_.id).map(_._1).sortBy(_.desc).take(pageSize)
        db.run(query.result)
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
}
