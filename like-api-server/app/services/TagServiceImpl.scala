package services

import javax.inject.Inject

import com.likeorz.models.{ Tag => Tg }
import com.likeorz.dao.{ MarksComponent, TagsComponent }
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class TagServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends TagService
    with TagsComponent with MarksComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def suggestTagsForUser(userId: Long): Future[Seq[Tg]] = {
    val mostUsedQuery = (for {
      (mark, tag) <- marks join tags on (_.tagId === _.id)
      if mark.userId === userId
    } yield tag).groupBy(_.id).map(x => (x._1, x._2.length)).sortBy(_._2.desc).map(_._1).take(20)

    val recentUsedQuery = (for {
      (mark, tag) <- marks join tags on (_.tagId === _.id)
      if mark.userId === userId
    } yield (mark, tag)).sortBy(_._1.created.desc).map(_._2.id).take(5)

    for {
      mostUsedIds <- db.run(mostUsedQuery.result)
      recentUsedIds <- db.run(recentUsedQuery.result)
      tags <- db.run(tags.filter(_.id inSet (mostUsedIds.toSet ++ recentUsedIds)).result)
    } yield {
      val (t1, t2) = tags.partition(t => recentUsedIds.contains(t.id.get))
      t1 ++ t2
    }
  }

  override def autoComplete(name: String): Future[Seq[Tg]] = {
    val query = (for {
      tag <- tags if tag.tagName startsWith name.toLowerCase
    } yield tag).take(10)

    db.run(query.result)
  }

  override def hotTags: Future[Seq[Tg]] = {
    val query = (for {
      tag <- tags
    } yield tag).sortBy(_.likes.desc).take(150)
    db.run(query.result).map(tags => scala.util.Random.shuffle(tags).take(15))
  }

}
