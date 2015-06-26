package utils

import play.api.libs.json.{ JsNull, JsObject, Json }
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object AVOSUtils {

  val AVOSCloudApplicationId = Play.current.configuration.getString("avoscloud.appId").get
  val AVOSCloudApplicationKey = Play.current.configuration.getString("avoscloud.appKey").get

  val AVOSCloudApplicationUrl = Play.current.configuration.getString("avoscloud.apiUrl").get

  def sendSmsCode(mobilePhoneNumber: String, zone: String): Future[Boolean] = {
    Logger.debug(s"[AVOSCloud] Debug send code to mobile: $mobilePhoneNumber, zone: $zone")
    WS.url(s"$AVOSCloudApplicationUrl/requestSmsCode")
      .withHeaders(
        "X-AVOSCloud-Application-Id" -> AVOSCloudApplicationId,
        "X-AVOSCloud-Application-Key" -> AVOSCloudApplicationKey,
        "Content-Type" -> "application/json"
      ).post(Json.obj("mobilePhoneNumber" -> mobilePhoneNumber, "countryCode" -> zone, "template" -> "smscode")).map(response => { println(response.body); response.status == 200 })
  }

  def verifySmsCode(mobilePhoneNumber: String, zone: String, code: String): Future[Boolean] = {
    Logger.debug(s"[AVOSCloud] Debug verifying SMS mobile: $mobilePhoneNumber, code: $code")
    WS.url(s"$AVOSCloudApplicationUrl/verifySmsCode/$code?mobilePhoneNumber=$mobilePhoneNumber&countryCode=$zone")
      .withHeaders(
        "X-AVOSCloud-Application-Id" -> AVOSCloudApplicationId,
        "X-AVOSCloud-Application-Key" -> AVOSCloudApplicationKey,
        "Content-Type" -> "application/json"
      ).post("").map(response => response.status == 200)
  }

  def installations(deviceToken: String, deviceType: String): Future[String] = {
    Logger.debug(s"[AVOSCloud] Debug installations token: $deviceToken, type: $deviceType")
    WS.url(s"$AVOSCloudApplicationUrl/installations")
      .withHeaders(
        "X-AVOSCloud-Application-Id" -> AVOSCloudApplicationId,
        "X-AVOSCloud-Application-Key" -> AVOSCloudApplicationKey,
        "Content-Type" -> "application/json")
      .post(Json.obj("deviceType" -> deviceType, "deviceToken" -> deviceToken))
      .map(response => (response.json \ "objectId").as[String])
  }

  def updateInstallations(objectId: String, deviceToken: String, deviceType: String): Future[String] = {
    Logger.debug(s"[AVOSCloud] Debug update installations token: $deviceToken, type: $deviceType")
    WS.url(s"$AVOSCloudApplicationUrl/installations/$objectId")
      .withHeaders(
        "X-AVOSCloud-Application-Id" -> AVOSCloudApplicationId,
        "X-AVOSCloud-Application-Key" -> AVOSCloudApplicationKey,
        "Content-Type" -> "application/json")
      .put(Json.obj("deviceType" -> deviceType, "deviceToken" -> deviceToken))
      .map(response => (response.json \ "objectId").as[String])
  }

  def pushNotificationLocal(targetId: String, alert: String, badge: Int, extra: JsObject = Json.obj()): Future[Boolean] = {
    Logger.debug(s"[AVOSCloud] Debug push notification")
    val body = Json.obj(
      "where" -> Json.obj("objectId" -> targetId),
      "data" -> extra.deepMerge(Json.obj("alert" -> Json.obj(
        "loc-key" -> "APNS_NewLike",
        "loc-args" -> Json.toJson(Seq("XXXX"))
      ), "badge" -> badge))
    )
    Logger.debug(Json.prettyPrint(body))
    WS.url(s"$AVOSCloudApplicationUrl/push")
      .withHeaders(
        "X-AVOSCloud-Application-Id" -> AVOSCloudApplicationId,
        "X-AVOSCloud-Application-Key" -> AVOSCloudApplicationKey,
        "Content-Type" -> "application/json; charset=utf-8"
      ).post(Json.stringify(body)).map(response => response.status == 200)
  }

  def pushNotification(targetId: String, alert: String, badge: Int, extra: JsObject = Json.obj()): Future[Boolean] = {
    Logger.debug(s"[AVOSCloud] Debug push notification")
    val body = Json.obj(
      "where" -> Json.obj("objectId" -> targetId),
      "data" -> extra.deepMerge(Json.obj("alert" -> alert, "badge" -> badge))
    )
    Logger.debug(Json.prettyPrint(body))
    WS.url(s"$AVOSCloudApplicationUrl/push")
      .withHeaders(
        "X-AVOSCloud-Application-Id" -> AVOSCloudApplicationId,
        "X-AVOSCloud-Application-Key" -> AVOSCloudApplicationKey,
        "Content-Type" -> "application/json; charset=utf-8"
      ).post(Json.stringify(body)).map(response => response.status == 200)
  }

}
