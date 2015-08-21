package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Report(
  id: Option[Long],
  userId: Long,
  postId: Long,
  created: Long = System.currentTimeMillis() / 1000,
  reason: Option[String] = None)
