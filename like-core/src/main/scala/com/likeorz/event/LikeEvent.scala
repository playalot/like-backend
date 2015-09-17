package com.likeorz.event

import org.joda.time.DateTime
import play.api.libs.json._

object LikeEventType {
  val publish = "publish"
  val mark = "mark"
  val like = "like"
  val recommendToAll = "recommend-to-all"
  val recommendToGroup = "recommend-to-Group"
}

case class LikeEvent(
    eventId: Option[String] = None,
    eventType: String,
    entityType: String,
    entityId: String,
    targetEntityType: Option[String] = None,
    targetEntityId: Option[String] = None,
    properties: JsObject = Json.obj(),
    eventTime: DateTime = DateTime.now) {

  override def toString: String = {
    s"Event(id=$eventId,eType=$entityType,eId=$entityId,tType=$targetEntityType,tId=$targetEntityId,p=$properties,t=$eventTime)"
  }
}

object LikeEvent {
  implicit val likeEventFormat = Json.format[LikeEvent]
}

