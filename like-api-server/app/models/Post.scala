package models

case class Post(
  id: Option[Long],
  content: String,
  description: Option[String],
  `type`: String = "PHOTO", // PHOTO, VIDEO, TEXT, MUSIC
  userId: Long,
  created: Long,
  updated: Long,
  tagId: Long,
  likes: Long)