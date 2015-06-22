package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Like(
  markId: Long,
  userId: Long,
  created: Long = System.currentTimeMillis / 1000)
