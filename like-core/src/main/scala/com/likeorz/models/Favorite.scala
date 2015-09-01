package com.likeorz.models

case class Favorite(
  userId: Long,
  postId: Long,
  created: Long = System.currentTimeMillis / 1000)
