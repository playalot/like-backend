package com.likeorz.models

case class Tag(
  id: Option[Long],
  name: String,
  usage: Long = 0,
  group: Long = 0) extends Identifier
