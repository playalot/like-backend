package com.likeorz.services

import com.likeorz.models.{ PromoteEntity, Entity }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/26/15
 */
trait PromoteService {

  def getEntity(id: Long): Future[Option[Entity]]

  def getEntitybyName(name: String): Future[Option[Entity]]

  def getPromoteEntities(num: Int): Future[Seq[Entity]]

  def getEntities(pageSize: Int, page: Int, filter: String): Future[Seq[(Entity, Option[PromoteEntity])]]

  def insertEntity(entity: Entity): Future[Entity]

  def promoteEntity(entityId: Long): Future[Unit]

  def unPromoteEntity(entityId: Long): Future[Int]

  def deleteEntity(entityId: Long): Future[Int]

  def updateEntity(entity: Entity): Future[Entity]
}
