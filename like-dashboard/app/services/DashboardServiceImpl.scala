package services

import javax.inject.Inject

import com.likeorz.dao._
import com.likeorz.models.{ Recommend, Post, User }
import com.likeorz.utils.KeyUtils
import models.Page
import org.nlpcn.commons.lang.jianfan.JianFan
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile
import utils.RedisCacheClient

import scala.concurrent.Future
import scala.util.Random

class DashboardServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DashboardService
    with PostsComponent with UsersComponent
    with TagsComponent with MarksComponent
    with LikesComponent with CommentsComponent
    with RecommendsComponent with FollowsComponent
    with ReportsComponent with DeletedPhotosComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def list(page: Int = 0, pageSize: Int = 36): Future[Page[(Post, Seq[(Long, String, Int)])]] = {
    val offset = pageSize * page
    val query =
      (for {
        post <- posts
      } yield post)
        .sortBy(_.created.desc)
        .drop(offset)
        .take(pageSize)

    for {
      totalRows <- db.run(posts.length.result)
      list = query.result.map { rows => rows.collect { case post => post } }
      result <- db.run(list)
      futures = result.map { post =>
        val cachedMarks = RedisCacheClient.zrevrangebyscore("post_mark:" + post.id.get, offset = 0, limit = 100).map(v => (v._1.toLong, v._2.toInt)).toMap

        if (cachedMarks.nonEmpty) {
          val markIds = cachedMarks.keySet.mkString(", ")
          val query = sql"""select m.id, t.tag from mark m inner join tag t on m.tag_id = t.id  where m.id in (#$markIds)""".as[(Long, String)]
          db.run(query).map { list =>
            val marks = list.map(row => (row._1, row._2, cachedMarks.getOrElse(row._1, 0)))
            (post, marks)
          }
        } else {
          Future.successful((post, Seq()))
        }
      }
      x <- Future.sequence(futures.toList)
    } yield {
      Page(x, page, offset, totalRows)
    }
  }

  override def blockPost(postId: Long, status: Boolean): Future[Boolean] = {
    if (status) {
      db.run(posts.filter(_.id === postId).map(x => x.score).update(0)).map(rs => rs == 1)
    } else {
      db.run(posts.filter(_.id === postId).map(x => x.score).update(-1)).map(rs => rs == 1)
    }
  }

  override def recommendPost(postId: Long, status: Boolean): Future[Unit] = {
    if (status) {
      db.run(recommends.filter(_.postId === postId).delete).map(_ => ())
    } else {
      val recommend = Recommend(None, postId)
      db.run(recommends returning recommends.map(_.id) += recommend).map(_ => ())
    }
  }

  override def isPostRecommended(postId: Long): Future[Boolean] = {
    db.run(recommends.filter(_.postId === postId).result.headOption).map(_.isDefined)
  }

  override def isPostBlocked(postId: Long): Future[Boolean] = {
    db.run(posts.filter(_.id === postId).result.headOption).map {
      case Some(post) => post.score.getOrElse(0) < 0
      case None       => false
    }
  }

}
