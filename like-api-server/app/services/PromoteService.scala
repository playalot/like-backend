package services

import com.likeorz.models.Entity

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/26/15
 */
trait PromoteService {

  def getEntity(id: Long): Future[Option[Entity]]

  def getEntitybyName(name: String): Future[Option[Entity]]

  def getPromoteEntities(num: Int): Future[Seq[Entity]]

}
