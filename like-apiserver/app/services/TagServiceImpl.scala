package services

import javax.inject.Inject

import dao.{ UsersComponent, TagsComponent, CommentConponent }
import models.{ User, Comment }
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class TagServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends TagService
    with TagsComponent with CommentConponent with UsersComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val tags = TableQuery[TagsTable]
  private val comments = TableQuery[CommentsTable]
  private val users = TableQuery[UsersTable]

  override def autoComplete(name: String): Future[Seq[models.Tag]] = {
    val query = (for {
      tag <- tags if tag.tagName startsWith name.toLowerCase
    } yield (tag)).take(10)

    db.run(query.result)
  }

  override def hotTags: Future[Seq[models.Tag]] = {
    val query = (for {
      tag <- tags
    } yield (tag)).sortBy(_.likes.desc).take(100)
    db.run(query.result).map(tags => scala.util.Random.shuffle(tags).take(15))
  }

  override def commentMark(comment: Comment): Future[Comment] = {
    db.run(comments returning comments.map(_.id) += comment).map(id => comment.copy(id = Some(id)))
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
}
