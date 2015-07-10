package com.likeorz.models

case class Notification(
  id: Option[Long],
  `type`: String, // LIKE, COMMENT, FOLLOW, MARK, REPLY
  userId: Long,
  fromUserId: Long,
  updated: Long,
  tagName: Option[String] = None,
  postId: Option[Long] = None,
  markId: Option[Long] = None)
