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

  def getMarkWithUserAndLikes(markId: Long, fromUserId: Option[Long]): Future[Option[(Mark, User, String, Int, Boolean)]]

  def getMarkWithTagName(markId: Long): Future[Option[(Mark, String)]]

  def getMarkWithPostAndTag(markId: Long): Future[Option[(Mark, Post, Tag)]]

  def countLikesForUser(userId: Long): Future[Long]

  def like(mark: Mark, post: Post, userId: Long): Future[Unit]

  def unlike(mark: Mark, post: Post, userId: Long): Future[Unit]

  def getLikes(markId: Long): Future[Seq[(Like, User)]]

  def checkLikes(userId: Long, markIds: Seq[Long]): Future[Seq[Long]]

  def getMarkPostTag(markId: Long): Future[Option[(Mark, Post, Tag)]]

  def commentMark(markId: Long, comment: Comment): Future[Comment]

  def deleteCommentFromMark(commentId: Long, userId: Long): Future[Boolean]

  def getCommentsForMark(markId: Long, order: String): Future[Seq[(Comment, User, Option[User])]]

  def deleteMark(markId: Long): Future[Unit]

}
