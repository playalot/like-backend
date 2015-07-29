package services

import com.mohiva.play.silhouette.api.services.IdentityService
import models.Admin

import scala.concurrent.Future

trait AdminService extends IdentityService[Admin] {

  def insert(email: String, password: String): Future[Admin]

  def stats: Future[Map[String, Long]]
}
