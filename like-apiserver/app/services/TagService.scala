package services

import models.{ Comment, Tag }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait TagService {

  def autoComplete(name: String): Future[Seq[Tag]]

  def hotTags: Future[Seq[Tag]]

  def commentTag(comment: Comment): Future[Comment]

  def commentFromTag(commentId: Long, userId: Long): Future[Boolean]

  def getCommentsForTag(postId: Long, created: Option[Long] = None): Future[Seq[Comment]]
}
