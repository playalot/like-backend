package com.likeorz.push

import play.api.libs.json.{ Json, JsObject }

/**
 * Created by Guan Guan
 * Date: 6/24/15
 */
case class PushNotification(targetId: String, alert: String, badge: Int, extra: JsObject = Json.obj())
