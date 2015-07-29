package com.likeorz.services

import com.google.inject.Inject
import com.likeorz.dao._
import com.likeorz.models.{ Tag => Tg, _ }
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
      (mark, post) <- marks join posts on (_.postId === _.id) if mark.id === markId
    } yield (mark, post)
    db.run(query.result.headOption)
  }

  override def getMarkWithUserAndLikes(markId: Long, fromUserId: Option[Long]): Future[Option[(Mark, User, String, Int, Boolean)]] = {
    val query = for {
      (mark, user) <- marks join users on (_.userId === _.id) if mark.id === markId
    } yield (mark, user)

    db.run(query.result.headOption).flatMap {
      case Some((mark, user)) =>
        for {
          tagOpt <- db.run(tags.filter(_.id === mark.tagId).result.headOption)
          likeNum <- db.run(likes.filter(_.markId === markId).length.result)
          isLiked <- { if (fromUserId.isDefined) db.run(likes.filter(l => l.markId === markId && l.userId === fromUserId.get).result.headOption) else Future.successful(None) }
        } yield {
          Some(mark, user, tagOpt.map(_.tagName).getOrElse(""), likeNum, isLiked.nonEmpty)
        }
      case _ => Future.successful(None)
    }
  }

  @deprecated("legacy", "1.1.0")
  override def getMarkWithTagName(markId: Long): Future[Option[(Mark, String)]] = {
    val query = for {
      (mark, tag) <- marks join tags on (_.tagId === _.id) if mark.id === markId
    } yield (mark, tag.tagName)
    db.run(query.result.headOption)
  }

  override def getMarkWithPostAndTag(markId: Long): Future[Option[(Mark, Post, Tg)]] = {
    val markQuery = for {
      ((mark, post), tag) <- marks join posts on (_.postId === _.id) join tags on (_._1.tagId === _.id)
      if mark.id === markId
    } yield (mark, post, tag)
    db.run(markQuery.result.headOption)
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

  override def like(mark: Mark, post: Post, userId: Long): Future[Unit] = {
    db.run(likes.filter(l => l.markId === mark.id.get && l.userId === userId).result.headOption).flatMap {
      case Some(like) => Future.successful(())
      case None =>
        db.run(likes += Like(mark.id.get, userId)).map { _ =>
          RedisCacheClient.zincrby(KeyUtils.postMark(post.id.get), 1, mark.identify)
          // Increate user info cache
          RedisCacheClient.hincrBy(KeyUtils.user(post.userId), "likes", 1)
          RedisCacheClient.zincrby(KeyUtils.pushLikes, 1, post.userId.toString)

          if (post.userId != mark.userId) {
            RedisCacheClient.hincrBy(KeyUtils.user(mark.userId), "likes", 1)
            // Increase like push cached which is to be pushed as notification to user
            RedisCacheClient.zincrby(KeyUtils.pushLikes, 1, mark.userId.toString)
          }
          ()
        }
    }
  }

  override def unlike(mark: Mark, post: Post, userId: Long): Future[Unit] = {
    db.run(likes.filter(l => l.markId === mark.id.get && l.userId === userId).delete).map { result =>
      if (result > 0) {
        RedisCacheClient.zincrby(KeyUtils.postMark(mark.postId), -1, mark.identify)
        if (RedisCacheClient.zscore(KeyUtils.postMark(mark.postId), mark.identify).getOrElse(-1.0) <= 0) {
          RedisCacheClient.zrem(KeyUtils.postMark(mark.postId), mark.identify)
          db.run(marks.filter(_.id === mark.id).delete)
        }
        // Decreate user info cache
        RedisCacheClient.hincrBy(KeyUtils.user(post.userId), "likes", -1)
        RedisCacheClient.zincrby(KeyUtils.pushLikes, -1, post.userId.toString)
        if (post.userId != mark.userId) {
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

  override def getMarkPostTag(markId: Long): Future[Option[(Mark, Post, Tg)]] = {
    val markQuery = for {
      ((mark, post), tag) <- marks join posts on (_.postId === _.id) join tags on (_._1.tagId === _.id) if mark.id === markId
    } yield (mark, post, tag)
    db.run(markQuery.result.headOption)
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
