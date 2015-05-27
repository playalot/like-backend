package models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Block(
  id: Long,
  userId: Long,
  blockedUserId: Long,
  created: Long)
