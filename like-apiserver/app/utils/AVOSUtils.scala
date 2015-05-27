package utils

import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

object AVOSUtils {

  val AVOSCloudApplicationId = Play.current.configuration.getString("avoscloud.appId").get
  val AVOSCloudApplicationKey = Play.current.configuration.getString("avoscloud.appKey").get

  val AVOSCloudApplicationUrl = Play.current.configuration.getString("avoscloud.apiUrl").get

  def verifySmsCode(mobilePhoneNumber: String, code: String): Future[Boolean] = {
    Logger.debug(s"[AVOSCloud] Debug verifying SMS mobile: $mobilePhoneNumber, code: $code")
    WS.url(s"$AVOSCloudApplicationUrl/verifySmsCode/$code?mobilePhoneNumber=$mobilePhoneNumber")
      .withHeaders(
        "X-AVOSCloud-Application-Id" -> AVOSCloudApplicationId,
        "X-AVOSCloud-Application-Key" -> AVOSCloudApplicationKey,
        "Content-Type" -> "application/json"
      ).post("").map(response => response.status == 200)
  }

}
