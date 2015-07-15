package com.likeorz.cluster.utils

import play.api.libs.json.Json
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object AVOSUtils {

  val AVOSCloudApplicationId = Play.current.configuration.getString("avoscloud.appId").get
  val AVOSCloudApplicationKey = Play.current.configuration.getString("avoscloud.appKey").get

  val AVOSCloudApplicationUrl = Play.current.configuration.getString("avoscloud.apiUrl").get

  def pushNotification(): Future[Boolean] = {
    Logger.debug(s"[AVOSCloud] Debug push notification")

    val body = Json.obj(
      "where" -> Json.obj(
        "objectId" -> "55573daee4b076f1c3914798"
      ),
      "data" -> Json.obj(
        "alert" -> "Like test notification",
        "badge" -> 5
      )
    )

    WS.url(s"$AVOSCloudApplicationUrl/push")
      .withHeaders(
        "X-AVOSCloud-Application-Id" -> AVOSCloudApplicationId,
        "X-AVOSCloud-Application-Key" -> AVOSCloudApplicationKey,
        "Content-Type" -> "application/json"
      ).post(body).map(response => response.status == 200)
  }

}
