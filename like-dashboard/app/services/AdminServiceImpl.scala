package services

import javax.inject.Inject

import com.likeorz.dao.RecommendsComponent
import com.likeorz.models.Recommend
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.{ Admin, AdminsComponent }
import play.api.db.slick._
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

class AdminServiceImpl @Inject() (passwordHasher: PasswordHasher,
  authInfoRepository: AuthInfoRepository,
  protected val dbConfigProvider: DatabaseConfigProvider)
    extends AdminService with AdminsComponent with RecommendsComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  def retrieve(loginInfo: LoginInfo): Future[Option[Admin]] = {
    db.run(admins.filter(_.email === loginInfo.providerKey).result.headOption)
  }

  override def insert(email: String, password: String): Future[Admin] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)
    val passwordInfo = passwordHasher.hash(password)
    db.run(admins.filter(_.email === email).result.headOption).flatMap {
      case Some(a) =>
        authInfoRepository.update(loginInfo, passwordInfo).map(_ => a)
      case None =>
        val admin = Admin(None, email, passwordInfo.password)
        db.run(admins returning admins.map(_.id) += admin).map(id => admin.copy(id = Some(id))).flatMap { a =>
          authInfoRepository.add(loginInfo, passwordInfo).map(_ => a)
        }
    }
  }

  override def stats: Future[Map[String, Long]] = {
    val query = sql"""SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema = DATABASE()""".as[(String, Long)]
    db.run(query).map(_.toMap)
  }

}
