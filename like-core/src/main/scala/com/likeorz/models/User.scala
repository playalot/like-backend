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
  created: Long = System.currentTimeMillis() / 1000,
  updated: Long = System.currentTimeMillis() / 1000,
  likes: Long = 0,
  refreshToken: Option[String] = None) extends Identifier