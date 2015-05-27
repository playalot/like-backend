package services

import javax.inject.Inject

import dao.TagsComponent
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class TagServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends TagService
    with TagsComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val tags = TableQuery[TagsTable]

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
}
