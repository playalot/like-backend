package com.likeorz.services

import javax.inject.Inject

import com.likeorz.dao.{ PromoteEntityComponent, EntityComponent }
import com.likeorz.models.{ PromoteEntity, Entity }
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.util.Random

/**
 * Created by Guan Guan
 * Date: 6/26/15
 */
class PromoteServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends PromoteService
    with PromoteEntityComponent with EntityComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def getEntity(id: Long): Future[Option[Entity]] = {
    db.run(entities.filter(_.id === id).result.headOption)
  }

  override def getEntitybyName(name: String): Future[Option[Entity]] = {
    db.run(entities.filter(_.name === name).result.headOption)
  }

  override def getPromoteEntities(num: Int): Future[Seq[Entity]] = {
    db.run(promoteEntities.sortBy(_.created.desc).map(_.entityId).result).flatMap { all =>
      val ids = Random.shuffle(all).take(num)
      db.run(entities.filter(_.id inSet ids).result)
    }
  }

  override def getEntities(pageSize: Int, page: Int, filter: String): Future[Seq[(Entity, Option[PromoteEntity])]] = {
    val query = (for {
      (entity, promoteOpt) <- entities joinLeft promoteEntities on (_.id === _.entityId)
      if entity.name like s"%$filter%"
    } yield (entity, promoteOpt)).drop(pageSize * page).take(pageSize)
    db.run(query.result)
  }

  override def insertEntity(entity: Entity): Future[Entity] = {
    db.run(entities returning entities.map(_.id) += entity).map { id =>
      entity.copy(id = Some(id))
    }
  }

  override def promoteEntity(entityId: Long): Future[Unit] = {
    db.run(promoteEntities += PromoteEntity(None, entityId)).map(_ => ())
  }

  override def unPromoteEntity(entityId: Long): Future[Int] = {
    db.run(promoteEntities.filter(_.entityId === entityId).delete)
  }

  override def deleteEntity(entityId: Long): Future[Int] = {
    for {
      p <- db.run(promoteEntities.filter(_.entityId === entityId).delete)
      e <- db.run(entities.filter(_.id === entityId).delete)
    } yield e
  }

  override def updateEntity(entity: Entity): Future[Int] = {
    db.run(entities.filter(_.id === entity.id).update(entity))
  }

}
