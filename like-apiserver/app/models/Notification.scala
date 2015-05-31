package models

case class Notification(
  id: Long,
  `type`: String, // LIKE, COMMENT, FOLLOW, MARK
  userId: Long,
  `new`: Boolean,
  postIds: Option[String],
  num: Int,
  fromUserId: Long,
  updated: Long)
