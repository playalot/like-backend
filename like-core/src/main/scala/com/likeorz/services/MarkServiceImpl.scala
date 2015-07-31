package com.likeorz.services

import com.google.inject.Inject
import com.likeorz.dao._
import com.likeorz.models._
import com.likeorz.utils.{ KeyUtils, RedisCacheClient }
import play.api.Logger
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/1/15
 */
class MarkServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends MarkService
    with PostsComponent with UsersComponent
    with MarksComponent with TagsComponent
    with LikesComponent with CommentsComponent
    with NotificationsComponent with FollowsComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def getMark(markId: Long): Future[Option[Mark]] = {
    db.run(marks.filter(_.id === markId).result.headOption)
  }

  override def getMarkWithPost(markId: Long): Future[Option[(Mark, Post)]] = {
    val query = for {
      mark <- marks
      post <- posts
      if mark.id === markId && mark.postId === post.id
    } yield (mark, post)
    db.run(query.result.headOption)
  }

  override def getMarkWithLikes(markId: Long): Future[Option[(Mark, Int)]] = {
    db.run(marks.filter(_.id === markId).take(1).result.headOption).flatMap {
      case Some(mark) =>
        RedisCacheClient.zscore(KeyUtils.postMark(mark.postId), markId.toString) match {
          case Some(number) =>
            Future.successful(Some(mark, number.toInt))
          case None =>
            val query = sql"""select count(1) FROM `like` l where l.mark_id=$markId""".as[Int]
            db.run(query).map { number =>
              Some(mark, number.head)
            }
        }
      case None => Future.successful(None)
    }
  }

  override def isLikedByUser(markId: Long, userId: Long): Future[Boolean] = {
    db.run(likes.filter(l => l.markId === markId && l.userId === userId).result.headOption).map(_.nonEmpty)
  }

  override def getMarkWithPostAuthor(markId: Long): Future[Option[(Mark, Long)]] = {
    db.run(marks.filter(_.id === markId).take(1).result.headOption).flatMap {
      case Some(mark) =>
        db.run(posts.filter(_.id === mark.postId).map(_.userId).take(1).result.head).map { author =>
          Some(mark, author)
        }
      case None => Future.successful(None)
    }
  }

  override def countLikesForUser(userId: Long): Future[Long] = {
    RedisCacheClient.hget(KeyUtils.user(userId), "likes") match {
      case Some(number) => Future.successful(number.toLong)
      case None =>
        val query = for {
          (like, mark) <- likes join marks on (_.markId === _.id) if mark.userId === userId
        } yield like
        db.run(query.length.result).map { number =>
          RedisCacheClient.hset(KeyUtils.user(userId), "likes", number.toString)
          val query = for { u <- users if u.id === userId } yield u.likes
          query.update(number)
          number
        }
    }
  }

  override def like(mark: Mark, postAuthorId: Long, userId: Long): Future[Unit] = {
    db.run(likes.filter(l => l.markId === mark.id.get && l.userId === userId).result.headOption).flatMap {
      case Some(like) => Future.successful(())
      case None =>
        db.run(likes += Like(mark.id.get, userId)).map { _ =>
          RedisCacheClient.zincrby(KeyUtils.postMark(mark.postId), 1, mark.identify)
          // Increate user info cache
          RedisCacheClient.hincrBy(KeyUtils.user(postAuthorId), "likes", 1)
          RedisCacheClient.zincrby(KeyUtils.pushLikes, 1, postAuthorId.toString)

          if (postAuthorId != mark.userId) {
            RedisCacheClient.hincrBy(KeyUtils.user(mark.userId), "likes", 1)
            // Increase like push cached which is to be pushed as notification to user
            RedisCacheClient.zincrby(KeyUtils.pushLikes, 1, mark.userId.toString)
          }
          ()
        }
    }
  }

  override def unlike(mark: Mark, postAuthor: Long, userId: Long): Future[Unit] = {
    db.run(likes.filter(l => l.markId === mark.id.get && l.userId === userId).delete).map { result =>
      if (result > 0) {
        RedisCacheClient.zincrby(KeyUtils.postMark(mark.postId), -1, mark.identify)
        if (RedisCacheClient.zscore(KeyUtils.postMark(mark.postId), mark.identify).getOrElse(-1.0) <= 0) {
          RedisCacheClient.zrem(KeyUtils.postMark(mark.postId), mark.identify)
          db.run(marks.filter(_.id === mark.id).delete)
        }
        // Decreate user info cache
        RedisCacheClient.hincrBy(KeyUtils.user(postAuthor), "likes", -1)
        RedisCacheClient.zincrby(KeyUtils.pushLikes, -1, postAuthor.toString)
        if (postAuthor != mark.userId) {
          RedisCacheClient.hincrBy(KeyUtils.user(mark.userId), "likes", -1)
          RedisCacheClient.zincrby(KeyUtils.pushLikes, -1, mark.userId.toString)
        }
        ()
      }
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
    db.run(comments returning comments.map(_.id) += comment).map(id => comment.copy(id = Some(id)))
  }

  override def deleteCommentFromMark(commentId: Long, userId: Long): Future[Boolean] = {
    db.run(comments.filter(c => c.id === commentId && c.userId === userId).delete).map(_ > 0)
  }

  override def getCommentsForMark(markId: Long, order: String): Future[Seq[(Comment, User, Option[User])]] = {
    val query = if (order == "desc") {
      (for {
        ((comment, user), reply) <- comments join users on (_.userId === _.id) joinLeft users on (_._1.replyId === _.id)
        if comment.markId === markId
      } yield (comment, user, reply)).sortBy(_._1.created.desc)
    } else {
      (for {
        ((comment, user), reply) <- comments join users on (_.userId === _.id) joinLeft users on (_._1.replyId === _.id)
        if comment.markId === markId
      } yield (comment, user, reply)).sortBy(_._1.created)
    }
    db.run(query.result)
  }

  override def deleteMark(markId: Long): Future[Unit] = {
    val query = for {
      (mark, post) <- marks join posts on (_.postId === _.id) if mark.id === markId
    } yield (mark, post)

    db.run(query.result.headOption).flatMap {
      case Some((mark, post)) =>
        for {
          l <- db.run(likes.filter(_.markId === markId).delete)
          c <- db.run(comments.filter(_.markId === markId).delete)
          m <- db.run(marks.filter(_.id === markId).delete)
        } yield {
          RedisCacheClient.hincrBy(KeyUtils.user(mark.userId), "likes", -l)
          if (mark.userId != post.userId) {
            RedisCacheClient.hincrBy(KeyUtils.user(post.userId), "likes", -l)
          }
          RedisCacheClient.zrem(KeyUtils.postMark(post.id.get), markId.toString)
          ()
        }
      case None => Future.successful(())
    }
  }

}
