package models

import play.api.db.slick.HasDatabaseConfig
import slick.driver.JdbcProfile

trait AdminsComponent { self: HasDatabaseConfig[JdbcProfile] =>
  import driver.api._

  class AdminsTable(tag: Tag) extends Table[Admin](tag, "admin_user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")
    def password = column[String]("password_hash")
    def created = column[Long]("created_at")
    def updated = column[Long]("updated_at")
    override def * = (id.?, email, password, created, updated) <> (Admin.tupled, Admin.unapply _)
  }

  protected val admins = TableQuery[AdminsTable]

  class AdminPasswordInfos(tag: Tag) extends Table[AdminPasswordInfo](tag, "admin_passwordinfo") {
    def loginInfoId = column[Long]("login_info_id")
    def hasher = column[String]("hasher")
    def password = column[String]("password")
    def salt = column[String]("salt")
    def * = (loginInfoId, hasher, password, salt.?) <> (AdminPasswordInfo.tupled, AdminPasswordInfo.unapply _)
  }

  protected val adminPasswordInfos = TableQuery[AdminPasswordInfos]
}
