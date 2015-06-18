package utils

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

  def sendSmsCode(mobilePhoneNumber: String, zone: String): Future[Boolean] = {
    Logger.debug(s"[AVOSCloud] Debug send code to mobile: $mobilePhoneNumber, zone: $zone")
    WS.url(s"$AVOSCloudApplicationUrl/requestSmsCode")
      .withHeaders(
        "X-AVOSCloud-Application-Id" -> AVOSCloudApplicationId,
        "X-AVOSCloud-Application-Key" -> AVOSCloudApplicationKey,
        "Content-Type" -> "application/json"
      ).post(Json.obj("mobilePhoneNumber" -> mobilePhoneNumber, "countryCode" -> zone)).map(response => { println(response.body); response.status == 200 })
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

}
