package services

import com.google.inject.Inject
import dao._
import models._
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile
import utils.RedisCacheClient

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/1/15
 */
class MarkServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends MarkService
    with PostsComponent
    with MarksComponent
    with CommentsComponent
    with TagsComponent
    with LikesComponent
    with NotificationsComponent
    with UsersComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val posts = TableQuery[PostsTable]
  private val marks = TableQuery[MarksTable]
  private val comments = TableQuery[CommentsTable]
  private val tags = TableQuery[TagsTable]
  private val likes = TableQuery[LikesTable]
  private val notifications = TableQuery[NotificationsTable]
  private val users = TableQuery[UsersTable]

  override def getMark(markId: Long): Future[Option[Mark]] = {
    db.run(marks.filter(_.id === markId).result.headOption)
  }

  override def getMarkWithTagName(markId: Long): Future[Option[(Mark, String)]] = {
    val query = for {
      (mark, tag) <- marks join tags on (_.tagId === _.id) if mark.id === markId
    } yield (mark, tag.tagName)
    db.run(query.result.headOption)
  }

  override def like(markId: Long, userId: Long): Future[Unit] = {
    val markQuery = for {
      ((mark, post), tag) <- marks join posts on (_.postId === _.id) join tags on (_._1.tagId === _.id)
      if mark.id === markId
    } yield (mark, post, tag)

    db.run(markQuery.result.headOption).flatMap {
      case Some(row) =>
        val (mark, post, tag) = row
        db.run(likes.filter(l => l.markId === markId && l.userId === userId).result.headOption).flatMap {
          case Some(like) => Future.successful(())
          case None =>
            db.run(likes += Like(markId, userId)).map { _ =>
              if (mark.userId != userId) {
                val notifyMarkUser = Notification(None, "LIKE", mark.userId, userId, System.currentTimeMillis / 1000, Some(tag.tagName), post.id)
                db.run(notifications += notifyMarkUser)
              }
              if (post.userId != mark.userId && post.userId != userId) {
                val notifyPostUser = Notification(None, "LIKE", post.userId, userId, System.currentTimeMillis / 1000, Some(tag.tagName), post.id)
                db.run(notifications += notifyPostUser)
              }
              RedisCacheClient.zIncrBy("post_mark:" + mark.postId, 1, mark.identify)
              RedisCacheClient.zIncrBy("tag_likes", 1, mark.tagId.toString)
              RedisCacheClient.zIncrBy("user_likes", 1, post.userId.toString)
              ()
            }
        }
      case None => Future.successful(())
    }
  }

  override def unlike(markId: Long, userId: Long): Future[Unit] = {
    val markQuery = for {
      ((mark, post), tag) <- marks join posts on (_.postId === _.id) join tags on (_._1.tagId === _.id)
      if mark.id === markId
    } yield (mark, post, tag)

    db.run(markQuery.result.headOption).flatMap {
      case Some(row) =>
        val (mark, post, tag) = row
        db.run(likes.filter(l => l.markId === markId && l.userId === userId).delete).map { result =>
          if (result > 0) {
            RedisCacheClient.zIncrBy("post_mark:" + mark.postId, -1, mark.identify)
            if (RedisCacheClient.zScore("post_mark:" + mark.postId, mark.identify).getOrElse(-1.0) <= 0) {
              RedisCacheClient.zRem("post_mark:" + mark.postId, mark.identify)
              db.run(marks.filter(_.id === mark.id).delete)
            }
            RedisCacheClient.zIncrBy("tag_likes", -1, mark.tagId.toString)
            RedisCacheClient.zIncrBy("user_likes", -1, post.userId.toString)
            db.run(notifications.filter(n => n.`type` === "LIKE" && n.fromUserId === userId && n.postId === post.id.get && n.tagName === tag.tagName).delete)
            ()
          }
        }
      case None => Future.successful(())
    }
  }

  override def getLikes(markId: Long): Future[Seq[(Like, User)]] = {
    val query = (for {
      (like, user) <- likes join users on (_.userId === _.id) if like.markId === markId
    } yield (like, user)).sortBy(_._1.created.desc)
    db.run(query.result)
  }

  override def checkLikes(userId: Long, markIds: Seq[Long]): Future[Seq[Long]] = {
    val query = for {
      like <- likes.filter(x => x.userId === userId && (x.markId inSet markIds))
    } yield like.markId
    db.run(query.result)
  }

  override def commentMark(markId: Long, comment: Comment): Future[Comment] = {
    val markQuery = for {
      ((mark, post), tag) <- marks join posts on (_.postId === _.id) join tags on (_._1.tagId === _.id) if mark.id === markId
    } yield (mark, post, tag)
    db.run(comments returning comments.map(_.id) += comment).map { id =>
      db.run(markQuery.result.headOption).map {
        case Some(row) =>
          val (mark, post, tag) = row
          if (comment.replyId.isDefined) {
            val notifyReplier = Notification(None, "REPLY", comment.replyId.get, comment.userId, System.currentTimeMillis / 1000, Some(tag.tagName), post.id)
            db.run(notifications += notifyReplier)
          } else {
            if (mark.userId != comment.userId) {
              val notifyMarkUser = Notification(None, "COMMENT", mark.userId, comment.userId, System.currentTimeMillis / 1000, Some(tag.tagName), post.id)
              db.run(notifications += notifyMarkUser)
            }
            if (post.userId != mark.userId && post.userId != comment.userId) {
              val notifyPostUser = Notification(None, "COMMENT", post.userId, comment.userId, System.currentTimeMillis / 1000, Some(tag.tagName), post.id)
              db.run(notifications += notifyPostUser)
            }
          }
        case None =>
      }
      comment.copy(id = Some(id))
    }
  }

  override def deleteCommentFromMark(commentId: Long, userId: Long): Future[Boolean] = {
    db.run(comments.filter(c => c.id === commentId && c.userId === userId).delete).map(_ > 0)
  }

  override def getCommentsForMark(markId: Long, pageSize: Int, created: Option[Long] = None): Future[Seq[(Comment, User, Option[User])]] = {
    val query = if (created.isDefined) {
      (for {
        ((comment, user), reply) <- comments join users on (_.userId === _.id) joinLeft users on (_._1.replyId === _.id)
        if comment.markId === markId && (comment.created < created.get)
      } yield (comment, user, reply)).sortBy(_._1.created.desc)
    } else {
      (for {
        ((comment, user), reply) <- comments join users on (_.userId === _.id) joinLeft users on (_._1.replyId === _.id)
        if comment.markId === markId
      } yield (comment, user, reply)).sortBy(_._1.created.desc)
    }
    db.run(query.result)
  }

  override def deleteMark(markId: Long, userId: Long): Future[Unit] = ???

  override def rebuildMarkCache(): Unit = {

    val query = for {
      (like, mark) <- likes join marks on (_.markId === _.id)
    } yield (like.markId, mark.postId)

    val publisher = db.stream(query.result)

    publisher.foreach { items =>
      RedisCacheClient.zIncrBy("post_mark:" + items._2, 1, items._1.toString)
      ()
    }
  }

}
