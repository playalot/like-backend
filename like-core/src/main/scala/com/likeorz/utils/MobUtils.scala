package com.likeorz.utils

import play.api.Play
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json

import scala.concurrent.Future

object MobUtils {

  val MobIOSApplicationKey = Play.current.configuration.getString("mob.ios.appKey").get
  val MobIOSApplicationUrl = Play.current.configuration.getString("mob.ios.apiUrl").get

  val MobAndroidApplicationKey = Play.current.configuration.getString("mob.android.appKey").get
  val MobApplicationUrl = Play.current.configuration.getString("mob.android.apiUrl").get

  @deprecated("Mob upgrade", "v1.2.0")
  def verifySmsCodeIOS(mobilePhoneNumber: String, zone: String, code: String): Future[Boolean] = {
    var client: MobClient = null
    Future {
      try {
        client = new MobClient(MobIOSApplicationUrl)

        client.addParam("appkey", MobIOSApplicationKey)
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

  def verifySmsCodeIOS2(mobilePhoneNumber: String, zone: String, code: String): Future[Boolean] = {
    var client: MobClient = null
    Future {
      try {
        client = new MobClient(MobApplicationUrl)

        client.addParam("appkey", MobIOSApplicationKey)
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

  def verifySmsCodeAndroid(mobilePhoneNumber: String, zone: String, code: String): Future[Boolean] = {
    var client: MobClient = null
    Future {
      try {
        client = new MobClient(MobApplicationUrl)

        client.addParam("appkey", MobAndroidApplicationKey)
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
