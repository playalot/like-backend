package com.likeorz.models

case class Like(
  markId: Long,
  userId: Long,
  created: Long = System.currentTimeMillis / 1000)
