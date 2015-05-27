package services

import javax.inject.Inject

import dao._
import models.{ Post, User }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
class PostServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends PostService
    with PostsComponent with UsersComponent with TagsComponent with MarksComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val posts = TableQuery[PostsTable]
  private val tags = TableQuery[TagsTable]
  private val marks = TableQuery[MarksTable]
  private val users = TableQuery[UsersTable]

  override def countByUserId(userId: Long): Future[Long] = {
    db.run(posts.filter(_.userId === userId).result.map(_.length))
  }

  override def searchByTag(page: Int = 0, pageSize: Int = 20, name: String = "%"): Future[Seq[(Post, User)]] = {
    val offset = pageSize * page
    val query = (for {
      (((tag, mark), posts), users) <- tags join marks on (_.id === _.tagId) join posts on (_._2.postId === _.id) join users on (_._2.userId === _.id)
      if (tag.tagName.toLowerCase like name.toLowerCase)
    } yield (posts, users))
      .sortBy(_._1.likes.desc)
      .drop(offset)
      .take(pageSize)
    db.run(query.result)
  }

}
