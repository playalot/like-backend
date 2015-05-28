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

  override def searchByTag(page: Int = 0, pageSize: Int = 20, name: String = "%"): Future[Seq[(Post, User)]] = {
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

  override def getMarksForPost(postId: Long, userId: Option[Long] = None): Future[(Seq[(Long, String)], Set[Long], Map[Long, Int], Seq[(Comment, User, Option[User])])] = {
    val cachedMarks = RedisCacheClient.zRevRangeByScore("post_mark:" + postId).map(v => (v._1.toLong, v._2.toInt)).toMap
    Logger.debug("Cached marks: " + cachedMarks)

    val marksQuery = for {
      (mark, tag) <- marks join tags on (_.tagId === _.id)
      if (mark.postId === postId) && (mark.id inSet cachedMarks.keySet)
    } yield (mark.id, tag.tagName)

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

}
