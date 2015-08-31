package com.likeorz.models

case class UserTag(
  userId: Long,
  tagId: Long,
  subscribe: Boolean = true)
