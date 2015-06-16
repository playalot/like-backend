package utils

import play.api.libs.ws.WS
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/15/15
 */
object MobSmsUtils {

  val MobSmsApplicationKey = Play.current.configuration.getString("mob.appKey").get
  val MobSmsApplicationUrl = Play.current.configuration.getString("mob.apiUrl").get

  def verifySmsCode(mobilePhoneNumber: String, zone: String, code: String): Future[Boolean] = {
    Logger.debug(s"[MobSms] Debug verifying SMS mobile: $mobilePhoneNumber, zone: $zone, code: $code")
    WS.url(MobSmsApplicationUrl).post(Map(
      "appkey" -> Seq(MobSmsApplicationKey),
      "phone" -> Seq(mobilePhoneNumber),
      "zone" -> Seq(zone),
      "code" -> Seq(code))).map(response => { println(response.json); (response.json \ "status").asOpt[Int] == Some(200) })
  }
}
