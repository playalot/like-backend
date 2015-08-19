package models

case class AdminPasswordInfo(
  loginInfoId: Long,
  hasher: String,
  password: String,
  salt: Option[String])
