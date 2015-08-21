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
import scala.collection.JavaConversions._

class TagServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, configuration: Configuration) extends TagService
    with TagsComponent with MarksComponent with UsersComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val illegalWords = configuration.getStringList("tag.illegal-words").get.toList

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
      val result = t1.map(_.name) ++ t2.map(_.name)
      if (result.size < 20) (result ++ recommendTags.toSeq.take(20 - result.size), recommendTags.toSeq.take(20 - result.size))
      else (result, Seq())
    }
  }

  override def autoComplete(name: String): Future[Seq[Tg]] = {
    val query = (for {
      tag <- tags if tag.name startsWith name.toLowerCase
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
        Random.shuffle(tags.map(_.name)).take(num)
      }
    }
  }

  override def hotUsersForTag(tag: String, num: Int): Future[Seq[User]] = {
    val futureIds = RedisCacheClient.hget(KeyUtils.hotTagsWithUsers, tag) match {
      case Some(ids) => Future.successful(ids.split(",").filter(_.length > 0).map(_.toLong).toSeq)
      case None =>
        val query = sql"""SELECT DISTINCT m.user_id FROM mark m INNER JOIN post p ON m.post_id=p.id WHERE m.tag='#$tag' AND p.user_id=m.user_id ORDER BY m.created LIMIT 100""".as[Long]
        query.statements.foreach(println)
        db.run(query)
    }
    futureIds.flatMap { pool =>
      val userIds = Random.shuffle(pool).take(num)
      db.run(users.filter(_.id inSet userIds).result)
    }
  }

  override def validTag(tag: String): Boolean = {
    val illegalWords = configuration.getStringList("tag.illegal-words").get.toList
    !illegalWords.exists(w => tag.contains(w))
  }

}
