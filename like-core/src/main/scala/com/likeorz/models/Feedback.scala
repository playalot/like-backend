package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Feedback(
  id: Option[Long],
  userId: Long,
  feedback: String,
  created: Long = System.currentTimeMillis() / 1000)
