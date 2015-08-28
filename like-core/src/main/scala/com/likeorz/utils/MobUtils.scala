package com.likeorz.utils

import play.api.{ Logger, Play }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json

import scala.concurrent.Future

object MobUtils {

  val MobApplicationKey = Play.current.configuration.getString("mob.appKey").get
  val MobApplicationUrl = Play.current.configuration.getString("mob.apiUrl").get

  def verifySmsCode(mobilePhoneNumber: String, zone: String, code: String): Future[Boolean] = {
    var client: MobClient = null
    Future {
      try {
        client = new MobClient(MobApplicationUrl)

        client.addParam("appkey", MobApplicationKey)
          .addParam("phone", mobilePhoneNumber)
          .addParam("zone", zone)
          .addParam("code", code)
        client.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
        client.addRequestProperty("Accept", "application/json")
        val result = client.post()
        (Json.parse(result) \ "status").as[Int] == 200
      } finally {
        client.release()
      }
    }
  }

}
