package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Block(
  id: Option[Long] = None,
  userId: Long,
  blockedUserId: Long,
  created: Long = System.currentTimeMillis / 1000)
