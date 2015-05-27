package dao

import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */

trait TagsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class TagsTable(tag: Tag) extends Table[models.Tag](tag, "tag") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def tagName = column[String]("tag")
    def userId = column[Long]("user_id")
    def created = column[Long]("created")
    def updated = column[Long]("updated")
    def likes = column[Long]("likes")

    override def * = (id, tagName, userId, created, updated, likes) <> (models.Tag.tupled, models.Tag.unapply _)
  }
}

/*

trait TagDAO {
  def autoComplete(name: String): Future[Seq[String]]
}

class TagDAOImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends TagDAO with TagsComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val tags = TableQuery[TagsTable]

  override def autoComplete(name: String): Future[Seq[String]] = {
    val query = (for {
      tag <- tags if tag.tagName startsWith name.toLowerCase
    } yield (tag.tagName, tag.likes)).sortBy(_._2.desc).take(10)

    db.run(query.result) map (rows => rows.map { case (name, likes) => name })
  }

}
*/ 