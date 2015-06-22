package services

import models.{ Comment, User, Mark, Like }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/1/15
 */
trait MarkService {

  def getMark(markId: Long): Future[Option[Mark]]

  def getMarkWithTagName(markId: Long): Future[Option[(Mark, String)]]

  def like(markId: Long, userId: Long): Future[Unit]

  def unlike(markId: Long, userId: Long): Future[Unit]

  def getLikes(markId: Long): Future[Seq[(Like, User)]]

  def checkLikes(userId: Long, markIds: Seq[Long]): Future[Seq[Long]]

  def commentMark(markId: Long, comment: Comment): Future[Comment]

  def deleteCommentFromMark(commentId: Long, userId: Long): Future[Boolean]

  def getCommentsForMark(markId: Long, pageSize: Int = 1000, created: Option[Long] = None): Future[Seq[(Comment, User, Option[User])]]

  def deleteMark(markId: Long): Future[Unit]

  def rebuildMarkCache(): Future[Unit]
}
