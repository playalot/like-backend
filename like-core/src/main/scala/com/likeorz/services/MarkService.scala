package com.likeorz.services

import com.likeorz.models._

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/1/15
 */
trait MarkService {

  def getMark(markId: Long): Future[Option[Mark]]

  def getMarkWithPost(markId: Long): Future[Option[(Mark, Post)]]

  def getMarkWithLikes(markId: Long): Future[Option[(Mark, Int)]]

  def getMarkWithPostAuthor(markId: Long): Future[Option[(Mark, Long)]]

  def countLikesForUser(userId: Long): Future[Long]

  def like(mark: Mark, postAuthorId: Long, userId: Long): Future[Unit]

  def unlike(mark: Mark, postAuthor: Long, userId: Long): Future[Unit]

  def getLikes(markId: Long): Future[Seq[(Like, User)]]

  def checkLikes(userId: Long, markIds: Seq[Long]): Future[Seq[Long]]

  def isLikedByUser(markId: Long, userId: Long): Future[Boolean]

  def commentMark(markId: Long, comment: Comment): Future[Comment]

  def deleteCommentFromMark(commentId: Long, userId: Long): Future[Boolean]

  def getCommentsForMark(markId: Long, order: String): Future[Seq[(Comment, UserInfo, Option[UserInfo])]]

  def deleteMark(markId: Long): Future[Unit]

}
