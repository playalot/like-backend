package com.likeorz.services

import javax.inject.Inject

import com.likeorz.models.{ Tag => Tg, UserTag, TagGroup, UserInfo }
import com.likeorz.dao._
import com.likeorz.utils.{ RedisCacheClient, KeyUtils }
import play.api.Configuration
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.util.Random
import scala.collection.JavaConversions._

class TagServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, configuration: Configuration) extends TagService
    with TagsComponent with MarksComponent
    with UsersComponent with UserInfoComponent
    with UserTagsComponent with TagGroupsComponent
    with PostsComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  val illegalWords = configuration.getStringList("tag.illegal-words").get.toList

  override def suggestTagsForUser(userId: Long): Future[(Seq[String], Seq[String])] = {

    //    val mostUsedQuery = marks.filter(_.userId === userId).map(_.tagId).groupBy(x => x).map(x => (x._1, x._2.length)).sortBy(_._2.desc).map(_._1).take(20)
    //    val recentUsedQuery = marks.filter(_.userId === userId).sortBy(_.created.desc).map(_.tagId).take(5)
    //    mostUsedQuery.result.statements.foreach(println)
    //    recentUsedQuery.result.statements.foreach(println)
    val mostUsedQuery = (for {
      (mark, post) <- marks join posts on (_.postId === _.id) if mark.userId === userId && post.userId === userId
    } yield mark.tagName).groupBy(x => x).map(x => (x._1, x._2.length)).sortBy(_._2.desc).map(_._1).take(20)

    val recentUsedQuery = (for {
      (mark, post) <- marks join posts on (_.postId === _.id) if mark.userId === userId && post.userId === userId
    } yield mark).sortBy(_.created.desc).map(_.tagName).distinct.take(10)

    for {
      recommendTags <- db.run(sql"""SELECT tag FROM recommend_tags""".as[String])
      mostUsedTags <- db.run(mostUsedQuery.result)
      recentUsedTags <- db.run(recentUsedQuery.result)
    } yield {
      val result = recentUsedTags ++ mostUsedTags.filterNot(t => recentUsedTags.contains(t))
      if (result.size < 20) (result ++ recommendTags.toSeq.take(20 - result.size), recommendTags.toSeq.take(20 - result.size))
      else (result, Seq())
    }
  }

  override def autoComplete(name: String): Future[Seq[Tg]] = {
    val query = (for {
      tag <- tags if tag.name startsWith name.toLowerCase
    } yield tag).sortBy(_.usage.desc).take(5)
    db.run(query.result)
  }

  override def hotTags(num: Int): Future[Seq[String]] = {
    val pool = RedisCacheClient.hkeys(KeyUtils.hotTagsWithUsers).toSeq
    if (pool.nonEmpty) {
      Future.successful(Random.shuffle(pool).take(num))
    } else {
      db.run(tags.sortBy(_.usage.desc).take(120).result).map { tags =>
        Random.shuffle(tags.map(_.name)).take(num)
      }
    }
  }

  override def hotUsersForTag(tag: String, num: Int): Future[Seq[UserInfo]] = {
    val futureIds = RedisCacheClient.hget(KeyUtils.hotTagsWithUsers, tag) match {
      case Some(ids) => Future.successful(ids.split(",").filter(_.length > 0).map(_.toLong).toSeq)
      case None =>
        val query = sql"""SELECT DISTINCT m.user_id FROM mark m INNER JOIN post p ON m.post_id=p.id WHERE m.tag='#$tag' AND p.user_id=m.user_id ORDER BY m.created LIMIT 100""".as[Long]
        db.run(query)
    }
    futureIds.flatMap { pool =>
      val userIds = Random.shuffle(pool).take(num)
      db.run(userinfo.filter(_.id inSet userIds).result)
    }
  }

  override def validTag(tag: String): Boolean = {
    val illegalWords = configuration.getStringList("tag.illegal-words").get.toList
    !illegalWords.exists(w => tag.contains(w))
  }

  override def getTagGroups: Future[Seq[TagGroup]] = {
    db.run(tagGroups.result)
  }

  override def getGroupedTags: Future[Map[TagGroup, Seq[Tg]]] = {
    db.run(tagGroups.result).flatMap { groups =>
      val groupIds = groups.map(_.id.get)
      db.run(tags.filter(_.group inSet groupIds).result).map { tgs =>
        val groupedTags = tgs.groupBy(_.group)
        groups.map { group =>
          (group, groupedTags.getOrElse(group.id.get, Seq()))
        }.toMap
      }
    }
  }

  override def getTagsForGroup(groupId: Long, pageSize: Int, page: Int): Future[Seq[Tg]] = {
    db.run(tags.filter(_.group === groupId).sortBy(_.usage.desc).drop(pageSize * page).take(pageSize).result)
  }

  override def setTagGroup(tagId: Long, groupId: Long): Future[Unit] = {
    db.run(tags.filter(_.id === tagId).map(_.group).update(groupId)).map(_ => ())
  }

  override def addTagGroup(name: String): Future[TagGroup] = {
    val tagGroup = TagGroup(None, name)
    db.run(tagGroups returning tagGroups.map(_.id) += tagGroup).map(id => tagGroup.copy(id = Some(id)))
  }

  override def getUserTag(userId: Long, tagId: Long): Future[Option[UserTag]] = {
    db.run(userTags.filter(t => t.userId === userId && t.tagId === tagId).result.headOption)
  }

  override def subscribeTag(userId: Long, tagId: Long): Future[UserTag] = {
    db.run(userTags.filter(t => t.userId === userId && t.tagId === tagId).result.headOption).flatMap {
      case Some(tag) =>
        if (!tag.subscribe) {
          db.run(userTags.filter(t => t.userId === userId && t.tagId === tagId).map(_.subscribe).update(true)).map(_ => tag.copy(subscribe = true))
        } else {
          Future.successful(tag)
        }
      case None =>
        val userTag = UserTag(userId, tagId)
        db.run(userTags += userTag).map(_ => userTag)
    }
  }

  override def subscribeTags(tags: Seq[UserTag]): Future[Unit] = {
    (userTags ++= tags).statements.foreach(println)
    db.run(userTags ++= tags).map(_ => ())
  }

  override def unsubscribeTag(userId: Long, tagId: Long): Future[Int] = {
    db.run(userTags.filter(t => t.userId === userId && t.tagId === tagId).map(_.subscribe).update(false))
  }

  override def getUserHistoryTagIds(userId: Long): Future[Seq[Long]] = {
    db.run(userTags.filter(ut => ut.userId === userId).map(_.tagId).result)
  }

  override def getUserSubscribeTag(userId: Long, tagId: Long): Future[Option[UserTag]] = {
    db.run(userTags.filter(t => t.userId === userId && t.tagId === tagId && t.subscribe === true).result.headOption)
  }

  override def getTagByName(tagName: String): Future[Option[Tg]] = {
    db.run(tags.filter(_.name === tagName).result.headOption)
  }

  override def getTagById(id: Long): Future[Option[Tg]] = {
    db.run(tags.filter(_.id === id).result.headOption)
  }

  override def getSubscriberIdsForTag(tagId: Long): Future[Seq[Long]] = {
    db.run(userTags.filter(ut => ut.tagId === tagId && ut.subscribe === true).map(_.userId).result)
  }

  override def getTagWithImage(tagName: String): Future[Option[(Tg, Option[String])]] = {
    db.run(tags.filter(_.name === tagName).result.headOption).flatMap {
      case Some(tag) =>
        val query = sql"""select p.content from post p inner join mark m on p.id=m.post_id where m.post_id in (select r.post_id from post p inner join recommend r on p.id=r.post_id) and m.tag_id=${tag.id.get} ORDER BY p.created desc limit 1""".as[String]
        db.run(query).map(url => Some(tag, url.headOption))
      case None => Future.successful(None)
    }
  }

  override def getRecommendTags: Future[Seq[String]] = {
    db.run(sql"""SELECT tag FROM recommend_tags""".as[String])
  }

}
