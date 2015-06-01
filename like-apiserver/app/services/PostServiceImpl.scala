package services

import javax.inject.Inject

import dao._
import models.{ Comment, Post, User }
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
    with PostsComponent with UsersComponent with TagsComponent
    with MarksComponent with LikesComponent with CommentConponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val posts = TableQuery[PostsTable]
  private val tags = TableQuery[TagsTable]
  private val marks = TableQuery[MarksTable]
  private val likes = TableQuery[LikesTable]
  private val users = TableQuery[UsersTable]
  private val comments = TableQuery[CommentsTable]

  override def countByUserId(userId: Long): Future[Long] = {
    db.run(posts.filter(_.userId === userId).result.map(_.length))
  }

  override def searchByTag(page: Int = 0, pageSize: Int = 18, name: String = "%"): Future[Seq[(Post, User)]] = {
    val offset = pageSize * page

    val jian = JianFan.f2J(name)
    val fan = JianFan.j2F(name)

    val query = (for {
      (((tag, mark), post), user) <- tags join marks on (_.id === _.tagId) join posts on (_._2.postId === _.id) join users on (_._2.userId === _.id)
      if (tag.tagName.toLowerCase like jian.toLowerCase) || (tag.tagName.toLowerCase like fan.toLowerCase)
    } yield (post, user))
      .sortBy(_._1.likes.desc)
      .drop(offset)
      .take(pageSize)
    db.run(query.result)
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

    val marksQuery = for {
      ((mark, tag), user) <- marks join tags on (_.tagId === _.id) join users on (_._1.userId === _.id)
      if (mark.postId === postId) && (mark.id inSet cachedMarks.keySet)
    } yield (mark.id, tag.tagName, mark.created, user)

    val likesQuery = for {
      like <- likes.filter(x => x.userId === userId.getOrElse(0L) && (x.markId inSet cachedMarks.keySet))
    } yield (like.markId)

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

  override def deletePostById(postId: Long, userId: Long): Future[Either[Boolean, String]] = {
    val cachedMarks = RedisCacheClient.zRevRangeByScore("post_mark:" + postId, offset = 0, limit = 1000).map(v => (v._1.toLong, v._2.toInt)).toMap
    Logger.debug("Cached marks: " + cachedMarks)

    db.run(posts.filter(p => p.id === postId).result.headOption).flatMap {
      case Some(post) =>
        if (post.userId == userId)
          db.run(marks.filter(_.postId === postId).result).flatMap { markResults =>
            var total: Double = 0
            markResults.foreach { mark =>
              val likeNum = RedisCacheClient.zScore("post_mark:" + postId, mark.id.getOrElse(0).toString)
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
            } yield {
              Left(true)
            }
          }
        else
          Future.successful(Right("no.permission"))
      case None => Future.successful(Right("not.found"))
    }

  }

}
