package models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Comment(
  id: Option[Long],
  postId: Long,
  tagId: Long,
  userId: Long,
  replyId: Option[Long],
  comment: String,
  created: Long,
  location: Option[String])
