package com.likeorz.utils

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{ JsObject, Json }
import play.api.libs.ws._
import play.api.{ Logger, Play }

import scala.concurrent.Future

object MobUtils {

  val MobApplicationKey = Play.current.configuration.getString("mob.appKey").get
  val MobApplicationUrl = Play.current.configuration.getString("mob.apiUrl").get

  //  def verifySmsCode(mobilePhoneNumber: String, zone: String, code: String): Future[Boolean] = {
  //    Logger.debug(s"[Mob] Debug verifying SMS mobile: $mobilePhoneNumber, code: $code")
  //    WS.url(s"$MobApplicationUrl/verifySmsCode/$code?mobilePhoneNumber=$mobilePhoneNumber")
  //      .withHeaders("Content-Type" -> "application/json")
  //      .post(s"appkey=$MobApplicationKey&phone=$mobilePhoneNumber&zone=$zone&code=$code")
  //      .map(response => { println(response.body); response.status == 200 })
  //  }

  def verifySmsCode(mobilePhoneNumber: String, zone: String, code: String): Future[Boolean] = {
    var client: MobClient = null
    try {
      client = new MobClient(MobApplicationUrl)
      client.addParam("appkey", MobApplicationKey)
        .addParam("phone", mobilePhoneNumber)
        .addParam("zone", zone)
        .addParam("code", code)
      client.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
      client.addRequestProperty("Accept", "application/json")
      val result = client.post()
      Future.successful(result.size > 0)
    } finally {
      client.release()
    }

  }

}
