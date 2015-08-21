package com.likeorz.models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Tag(
  id: Option[Long],
  name: String,
  likes: Long = 1,
  usage: Long = 1) extends Identifier
