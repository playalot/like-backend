package models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Follow(
  id: Option[Long] = None,
  fromId: Long,
  toId: Long,
  both: Boolean = false,
  created: Long = System.currentTimeMillis / 1000)
