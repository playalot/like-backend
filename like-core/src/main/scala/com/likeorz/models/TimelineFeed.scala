package com.likeorz.models

import com.mongodb.casbah.Imports._
import play.api.libs.json.Json

case class TimelineFeed(postId: Long,
  reason: String,
  tag: Option[String] = None,
  category: Option[String] = None,
  ts: Long = System.currentTimeMillis() / 1000)

object TimelineFeed {

  implicit val feedRecordFormat = Json.format[TimelineFeed]

  val TypeMyPost = "my_post"
  val TypeBasedOnTag = "based_on_tag"
  val TypeEditorPick = "editor_pick"
  val TypeRecommend = "personal_recommend"

  def fromDBObject(row: MongoDBObject): TimelineFeed = {
    val postId = row.as[Long]("postId")
    val reason = row.as[String]("reason")
    val ts = row.as[Long]("ts")
    val tag = row.getAs[String]("tag")
    val category = row.getAs[String]("category")
    TimelineFeed(postId, reason, tag, category, ts)
  }

  def toDBObject(timelineFeed: TimelineFeed): MongoDBObject = {
    var obj = MongoDBObject(
      "postId" -> timelineFeed.postId,
      "reason" -> timelineFeed.reason,
      "ts" -> timelineFeed.ts
    )
    if (timelineFeed.tag.isDefined) {
      obj = obj += ("tag" -> timelineFeed.tag)
    }
    if (timelineFeed.category.isDefined) {
      obj = obj += ("category" -> timelineFeed.category)
    }
    obj
  }

}