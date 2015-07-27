package com.likeorz.models

case class Post(
  id: Option[Long],
  content: String,
  `type`: String = "PHOTO", // PHOTO, VIDEO, TEXT, MUSIC
  userId: Long,
  created: Long,
  updated: Long,
  tagId: Long,
  likes: Long,
  place: Option[String] = None,
  location: Option[String] = None,
  description: Option[String] = None,
  score: Option[Int] = Some(0)) extends Identifier
