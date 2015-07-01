package com.likeorz.dao

import com.likeorz.models.{ Entity, PromoteEntity }
import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

/**
 * Created by Guan Guan
 * Date: 6/26/15
 */
trait EntityComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class EntityTable(tag: Tag) extends Table[Entity](tag, "entity") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def description = column[String]("description")
    def avatar = column[String]("avatar")
    override def * = (id.?, name, description, avatar) <> (Entity.tupled, Entity.unapply _)
  }

  protected val entities = TableQuery[EntityTable]
}

trait PromoteEntityComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class PromoteEntityTable(tag: Tag) extends Table[PromoteEntity](tag, "promote_entity") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def entityId = column[Long]("entity_id")
    def created = column[Long]("created")

    override def * = (id.?, entityId, created) <> (PromoteEntity.tupled, PromoteEntity.unapply _)
  }

  protected val promoteEntities = TableQuery[PromoteEntityTable]
}
