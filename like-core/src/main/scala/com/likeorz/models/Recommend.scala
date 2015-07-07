package com.likeorz.models

case class Recommend(
  id: Option[Long],
  postId: Long,
  created: Long = System.currentTimeMillis() / 1000)
