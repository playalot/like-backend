package models

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import play.api.db.slick._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import slick.backend.DatabaseConfig
import slick.driver.JdbcProfile

import scala.concurrent.Future

class AdminPasswordInfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends DelegableAuthInfoDAO[PasswordInfo]
    with AdminsComponent with HasDatabaseConfig[JdbcProfile] {

  import driver.api._

  override protected val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

  /**
   * Finds the password info which is linked with the specified login info.
   *
   * @param loginInfo The linked login info.
   * @return The retrieved password info or None if no password info could be retrieved for the given login info.
   */
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = for {
    dbLoginInfoId <- db.run(admins.filter(_.email === loginInfo.providerKey).map(_.id).result.headOption)
    dbPasswordInfoOption <- db.run(adminPasswordInfos.filter(_.loginInfoId === dbLoginInfoId.getOrElse(-1L)).result.headOption)
  } yield dbPasswordInfoOption.map(dbPasswordInfo => PasswordInfo(dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt))

  /**
   * Saves the password info.
   * Though sub optimal, this code is readable
   * @param loginInfo The login info for which the password info should be saved.
   * @param passwordInfo The password info to save.
   * @return The saved password info or None if the password info couldn't be saved.
   */
  override def save(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] =
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, passwordInfo)
      case None    => add(loginInfo, passwordInfo)
    }

  /**
   * Adds password info to the database
   * @param loginInfo The login info to which the password info is attached
   * @param passwordInfo Added password info
   * @return a future with added password info
   */
  override def add(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(admins.filter(_.email === loginInfo.providerKey).map(_.id).result.head).flatMap { adminId =>
      db.run(adminPasswordInfos += AdminPasswordInfo(adminId, passwordInfo.hasher, passwordInfo.password, passwordInfo.salt))
    }.map(_ => passwordInfo)

  /**
   * Updates password info in the database
   * @param loginInfo The login info to which the password info is attached
   * @param passwordInfo Updated password info
   * @return a future with updated password info
   */
  override def update(loginInfo: LoginInfo, passwordInfo: PasswordInfo): Future[PasswordInfo] =
    db.run(admins.filter(_.email === loginInfo.providerKey).map(_.id).result.head).flatMap {
      loginInfoId => db.run(adminPasswordInfos.update(AdminPasswordInfo(loginInfoId, passwordInfo.hasher, passwordInfo.password, passwordInfo.salt)))
    }.map(_ => passwordInfo)

  /**
   * Removes corresponding password info
   * @param loginInfo The login info to which the password info is attached
   * @return an empty future
   */
  override def remove(loginInfo: LoginInfo): Future[Unit] =
    db.run(admins.filter(_.email === loginInfo.providerKey).map(_.id).result.head).flatMap {
      loginInfoId => db.run(adminPasswordInfos.filter(_.loginInfoId === loginInfoId).delete)
    }.map(_ => ())

}
