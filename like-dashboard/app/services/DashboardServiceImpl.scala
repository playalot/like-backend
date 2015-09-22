package services

import javax.inject.Inject

import com.likeorz.dao._
import com.likeorz.models.{ Recommend, Post }
import com.likeorz.utils.{ RedisCacheClient, KeyUtils }
import models.Page
import org.nlpcn.commons.lang.jianfan.JianFan
import play.api.db.slick.{ DatabaseConfigProvider, HasDatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

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
        val cachedMarks = RedisCacheClient.zrevrangeByScoreWithScores(KeyUtils.postMark(post.id.get)).map(v => (v._1.toLong, v._2.toInt)).toMap

        if (cachedMarks.nonEmpty) {
          val markIds = cachedMarks.keySet.mkString(", ")
          val query = sql"""select m.id, t.tag from mark m inner join tag t on m.tag_id = t.id  where m.id in (#$markIds)""".as[(Long, String)]
          db.run(query).map { list =>
            val marks = list.map(row => (row._1, row._2, cachedMarks.getOrElse(row._1, 0)))
            (post, marks)
          }
        } else {
          Future.successful((post, Seq.empty))
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

  override def recommendPost(postId: Long, status: Boolean): Future[Int] = {
    if (status) {
      db.run(recommends.filter(_.postId === postId).delete)
    } else {
      val recommend = Recommend(None, postId)
      db.run(recommends += recommend)
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

  override def countPostTotalLikes(filter: String): Future[Seq[(Long, Int)]] = {
    val query =
      sql"""SELECT DISTINCT p.id FROM post p INNER JOIN mark m ON p.id=m.post_id
           |INNER JOIN tag t ON m.tag_id=t.id WHERE t.tag='#$filter'""".as[Long]
    db.run(query).map { ids =>
      val scores = ids.map { pid =>
        val marks = RedisCacheClient.zrevrangeByScoreWithScores(KeyUtils.postMark(pid))
          .map(v => (v._1.toLong, v._2.toInt)).toMap
        (pid, marks.values.sum)
      }
      scores.sortWith(_._2 > _._2)
    }
  }

}
