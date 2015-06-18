package models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Report(
  id: Option[Long],
  userId: Long,
  postId: Long,
  num: Long = 1,
  created: Long = System.currentTimeMillis() / 1000)
