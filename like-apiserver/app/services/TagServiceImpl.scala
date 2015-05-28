package services

import javax.inject.Inject

import dao.{ TagsComponent, CommentConponent }
import models.Comment
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class TagServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends TagService
    with TagsComponent with CommentConponent with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val tags = TableQuery[TagsTable]
  private val comments = TableQuery[CommentsTable]

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

  override def commentTag(comment: Comment): Future[Comment] = {
    db.run(comments returning comments.map(_.id) += comment).map(id => comment.copy(id = Some(id)))
  }

  override def commentFromTag(commentId: Long, userId: Long): Future[Boolean] = {
    db.run(comments.filter(c => c.id === commentId && c.userId === userId).delete).map(_ > 0)
  }

  override def getCommentsForTag(markId: Long, created: Option[Long] = None): Future[Seq[Comment]] = {
    val query = (for {
      comment <- comments if comment.markId === markId
    } yield (comment)).sortBy(_.created)

    db.run(query.result)
  }
}
