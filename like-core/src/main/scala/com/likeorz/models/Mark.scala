package com.likeorz.models

case class Mark(
  id: Option[Long],
  postId: Long,
  tagId: Long,
  tagName: Option[String] = None,
  userId: Long,
  created: Long = System.currentTimeMillis / 1000) extends Identifier
