package services

import models.{ User, Comment, Tag }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait TagService {

  def autoComplete(name: String): Future[Seq[Tag]]

  def hotTags: Future[Seq[Tag]]

  def commentMark(comment: Comment): Future[Comment]

  def deleteCommentFromMark(commentId: Long, userId: Long): Future[Boolean]

  def getCommentsForMark(markId: Long, created: Option[Long] = None): Future[Seq[(Comment, User, Option[User])]]
}
