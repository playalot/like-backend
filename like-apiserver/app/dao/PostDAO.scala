package dao

import javax.inject.Inject

import models._
import play.api.Play
import play.api.db.slick.{ HasDatabaseConfigProvider, HasDatabaseConfig, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */

trait PostsComponent { self: HasDatabaseConfig[JdbcProfile] =>

  import driver.api._

  //  implicit val PostTypeEnumMapper = MappedColumnType.base[PostEnumType, String](
  //  { t =>
  //    t match {
  //      case PostPhoto => "PHOTO"
  //      case PostVideo => "VIDEO"
  //      case PostText => "TEXT"
  //      case PostMusic => "MUSIC"
  //    }
  //  }, { c =>
  //    c match {
  //      case 'A' => EnumValue1
  //      case 'B' => EnumValue2
  //      case _ => EnumValue3
  //    }
  //  }
  //  )
  class PostsTable(tag: Tag) extends Table[Post](tag, "post") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def description = column[String]("description")
    def `type` = column[String]("type")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")
    def updated = column[Long]("updated")
    def tagId = column[Long]("tag_id")
    def likes = column[Long]("likes")

    override def * = (id.?, content, description.?, `type`, userId, created, updated, tagId, likes) <> (Post.tupled, Post.unapply _)
  }
}

/*
trait PostDAO {

  def countByUserId(userId: Long): Future[Long]

  def searchByTag(page: Int = 0, pageSize: Int = 20, name: String = "%"): Future[Seq[(Post, User)]]

}

class PostDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends PostDAO with PostsComponent with UsersComponent with TagsComponent with MarksComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val posts = TableQuery[PostsTable]
  private val tags = TableQuery[TagsTable]
  private val marks = TableQuery[MarksTable]
  private val users = TableQuery[UsersTable]

  def countByUserId(userId: Long): Future[Long] = {
    db.run(posts.filter(_.userId === userId).result.map(_.length))
  }

  def searchByTag(page: Int = 0, pageSize: Int = 20, name: String = "%"): Future[Seq[(Post, User)]] = {
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
*/
