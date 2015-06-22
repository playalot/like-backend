package com.likeorz.models

case class Installation(
  id: Option[Long],
  userId: Long,
  objectId: String,
  deviceToken: String,
  deviceType: String, // ios, andriod
  status: Int,
  created: Long = System.currentTimeMillis() / 1000,
  updated: Long = System.currentTimeMillis() / 1000)
