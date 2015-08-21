package com.likeorz.models

import play.api.libs.json.Json

case class FeedRecord(postId: Long,
  reason: String,
  tag: Option[String],
  category: Option[String],
  timestamp: Long)

object FeedRecord {
  implicit val feedRecordFormat = Json.format[FeedRecord]
}