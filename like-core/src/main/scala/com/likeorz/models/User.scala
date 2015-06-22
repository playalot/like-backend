package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class User(
  id: Option[Long] = None,
  mobile: Option[String] = None,
  email: Option[String] = None,
  password: String,
  nickname: String,
  avatar: String,
  cover: String,
  created: Long,
  updated: Long,
  likes: Long,
  refreshToken: Option[String] = None) extends Identifier