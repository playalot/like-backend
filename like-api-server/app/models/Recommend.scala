package models

case class Recommend(
  id: Option[Long],
  postId: Long,
  created: Long)
