package com.likeorz.silhouettes

import com.likeorz.silhouettes.MobileProvider._
import com.likeorz.utils.{ MobUtils, AVOSUtils }
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.exceptions.AccessDeniedException
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

case class SmsCode(mobilePhoneNumber: String, zone: String, code: String)

class MobileProvider() extends Provider {

  /**
   * Gets the provider ID.
   *
   * @return The provider ID.
   */
  def id = ID

  def authenticateViaAvos(smsCode: SmsCode): Future[LoginInfo] = {
    val loginInfo = LoginInfo(id, smsCode.mobilePhoneNumber)
    if ((smsCode.mobilePhoneNumber == "66600000166" || smsCode.mobilePhoneNumber == "66666666610") && smsCode.code == "666666") {
      // This is a demo account
      Future.successful(loginInfo)
    } else {
      AVOSUtils.verifySmsCode(smsCode.mobilePhoneNumber, smsCode.zone, smsCode.code).map {
        case true  => loginInfo
        case false => throw new AccessDeniedException("Invalid sms code")
      }
    }
  }

  def authenticateViaMobIOS(smsCode: SmsCode): Future[LoginInfo] = {
    val loginInfo = LoginInfo(id, smsCode.mobilePhoneNumber)
    if ((smsCode.mobilePhoneNumber == "66600000166" || smsCode.mobilePhoneNumber == "66666666610") && smsCode.code == "666666") {
      // This is a demo account
      Future.successful(loginInfo)
    } else {
      MobUtils.verifySmsCodeIOS(smsCode.mobilePhoneNumber, smsCode.zone, smsCode.code).map {
        case true  => loginInfo
        case false => throw new AccessDeniedException("Invalid sms code")
      }
    }
  }

  def authenticateViaMobAndroid(smsCode: SmsCode): Future[LoginInfo] = {
    val loginInfo = LoginInfo(id, smsCode.mobilePhoneNumber)
    if ((smsCode.mobilePhoneNumber == "66600000166" || smsCode.mobilePhoneNumber == "66666666610") && smsCode.code == "666666") {
      // This is a demo account
      Future.successful(loginInfo)
    } else {
      MobUtils.verifySmsCodeAndroid(smsCode.mobilePhoneNumber, smsCode.zone, smsCode.code).map {
        case true  => loginInfo
        case false => throw new AccessDeniedException("Invalid sms code")
      }
    }
  }

}

/**
 * The companion object.
 */
object MobileProvider {

  /**
   * The provider constants.
   */
  val ID = "mobile"
}