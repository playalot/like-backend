package com.likeorz.services

import javax.inject.Inject

import com.likeorz.models.{ User, Tag => Tg }
import com.likeorz.dao.{ UsersComponent, MarksComponent, TagsComponent }
import com.likeorz.utils.{ RedisCacheClient, KeyUtils }
import org.joda.time.DateTime
import play.api.Configuration
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.util.Random

class TagServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, configuration: Configuration) extends TagService
    with TagsComponent with MarksComponent with UsersComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def suggestTagsForUser(userId: Long): Future[(Seq[String], Seq[String])] = {

    val mostUsedQuery = marks.filter(_.userId === userId).map(_.tagId).groupBy(x => x).map(x => (x._1, x._2.length)).sortBy(_._2.desc).map(_._1).take(20)

    val recentUsedQuery = marks.filter(_.userId === userId).sortBy(_.created.desc).map(_.tagId).take(5)

    for {
      recommendTags <- db.run(sql"""SELECT tag FROM recommend_tags""".as[String])
      mostUsedIds <- db.run(mostUsedQuery.result)
      recentUsedIds <- db.run(recentUsedQuery.result)
      tags <- db.run(tags.filter(_.id inSet (mostUsedIds.toSet ++ recentUsedIds)).result)
    } yield {
      val (t1, t2) = tags.partition(t => recentUsedIds.contains(t.id.get))
      val result = t1.map(_.tagName) ++ t2.map(_.tagName)
      if (result.size < 20) (result ++ recommendTags.toSeq.take(20 - result.size), recommendTags.toSeq.take(20 - result.size))
      else (result, Seq())
    }
  }

  override def autoComplete(name: String): Future[Seq[Tg]] = {
    val query = (for {
      tag <- tags if tag.tagName startsWith name.toLowerCase
    } yield tag).take(5)
    db.run(query.result)
  }

  override def hotTags(num: Int): Future[Seq[String]] = {
    val pool = RedisCacheClient.hkeys(KeyUtils.hotTagsWithUsers).toSeq
    if (pool.nonEmpty) {
      Future.successful(Random.shuffle(pool).take(num))
    } else {
      val query = (for {
        tag <- tags
      } yield tag).sortBy(_.likes.desc).take(120)
      db.run(query.result).map { tags =>
        Random.shuffle(tags.map(_.tagName)).take(num)
      }
    }
  }

  override def hotUsersForTag(tag: String, num: Int): Future[Seq[User]] = {
    val futureIds = RedisCacheClient.hget(KeyUtils.hotTagsWithUsers, tag) match {
      case Some(ids) => Future.successful(ids.split(",").filter(_.length > 0).map(_.toLong).toSeq)
      case None =>
        val ts7ago = DateTime.now().minusDays(7).getMillis / 1000
        val query = sql"""SELECT DISTINCT m.user_id FROM mark m INNER JOIN tag t ON m.tag_id=t.id WHERE t.tag='#$tag' ORDER BY m.created LIMIT 30""".as[Long]
        db.run(query)
    }
    futureIds.flatMap { pool =>
      val userIds = Random.shuffle(pool).take(num)
      db.run(users.filter(_.id inSet userIds).result)
    }
  }

  override def validTag(tag: String): Boolean = {
    import scala.collection.JavaConversions._
    val regexList = configuration.getStringList("tag-blacklist").get.toList
    !regexList.exists(r => tag.matches(r))
  }

}
