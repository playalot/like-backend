package models

import com.likeorz.models.Identifier
import com.mohiva.play.silhouette.api.Identity

case class Admin(
  id: Option[Long] = None,
  email: String,
  password: String,
  created: Long = System.currentTimeMillis() / 1000,
  updated: Long = System.currentTimeMillis() / 1000) extends Identifier with Identity
