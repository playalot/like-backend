package models

import com.mohiva.play.silhouette.api.Identity

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class User(
  id: Option[Long] = None,
  mobile: String,
  email: Option[String],
  password: String,
  nickname: String,
  avatar: String,
  cover: String,
  created: Long,
  updated: Long,
  likes: Long,
  refreshToken: Option[String] = None) extends Identity