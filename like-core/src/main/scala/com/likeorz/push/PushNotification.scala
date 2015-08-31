package com.likeorz.push

import play.api.libs.json.{ Json, JsObject }

case class PushNotification(targetId: String, alert: String, badge: Int, extra: JsObject = Json.obj())

case class JPushNotification(userIds: List[String], tags: List[String], alert: String, badge: Int, extra: Map[String, String] = Map())