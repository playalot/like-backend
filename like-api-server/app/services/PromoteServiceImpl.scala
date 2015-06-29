package services

import javax.inject.Inject

import com.likeorz.dao.{ PromoteEntityComponent, EntityComponent }
import com.likeorz.models.Entity
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/26/15
 */
class PromoteServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends PromoteService
    with PromoteEntityComponent with EntityComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val entities = TableQuery[EntityTable]
  private val promoteEntities = TableQuery[PromoteEntityTable]

  override def getEntity(id: Long): Future[Option[Entity]] = {
    db.run(entities.filter(_.id === id).result.headOption)
  }

  override def getEntitybyName(name: String): Future[Option[Entity]] = {
    db.run(entities.filter(_.name === name).result.headOption)
  }

  override def getPromoteEntities(): Future[Seq[Entity]] = {
    db.run(promoteEntities.sortBy(_.created.desc).map(_.entityId).result).flatMap { ids =>
      db.run(entities.filter(_.id inSet ids).result)
    }
  }
}
