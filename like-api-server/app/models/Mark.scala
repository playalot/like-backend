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
  created: Long = System.currentTimeMillis / 1000,
  updated: Long = System.currentTimeMillis / 1000,
  likes: Long = 1) extends Identifier