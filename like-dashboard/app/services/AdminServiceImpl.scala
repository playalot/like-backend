package services

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.likeorz.dao._
import com.likeorz.models._
import com.mohiva.play.silhouette.api.util.PasswordInfo
import models.{ Admin, AdminsComponent }
import play.api.db.slick._
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile
import scala.concurrent.Future

class AdminServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
    extends AdminService with AdminsComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  def retrieve(loginInfo: LoginInfo): Future[Option[Admin]] = {
    db.run(admins.filter(_.email === loginInfo.providerKey).result.headOption)
  }

}
