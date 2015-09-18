package com.likeorz.models

import play.api.libs.json.Json

case class TimelineFeed(postId: Long,
  reason: String,
  timestamp: Long = System.currentTimeMillis() / 1000,
  tag: Option[String] = None,
  category: Option[String] = None)

object TimelineFeed {

  implicit val feedRecordFormat = Json.format[TimelineFeed]

  val TypeMyPost = "my_post"
  val TypeBasedOnTag = "based_on_tag"
  val TypeEditorPick = "editor_pick"
  val TypeRecommend = "personal_recommend"
}