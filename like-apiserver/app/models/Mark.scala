package models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Mark(
  id: Option[Long],
  postId: Long,
  tagId: Long,
  userId: Long,
  created: Long,
  updated: Long,
  likes: Long) extends Identifier