package extensions

import MobileProvider._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.impl.exceptions.AccessDeniedException
import play.api.libs.concurrent.Execution.Implicits._
import utils.AVOSUtils

import scala.concurrent.Future

case class SmsCode(mobilePhoneNumber: String, code: String)

case class MobileInfo(mobilePhoneNumber: String, password: String, salt: Option[String] = None) extends AuthInfo

/**
 * Created by Guan Guan
 * Date: 5/22/15
 */
class MobileProvider() extends Provider {

  /**
   * Gets the provider ID.
   *
   * @return The provider ID.
   */
  def id = ID

  def authenticate(smsCode: SmsCode): Future[LoginInfo] = {
    val loginInfo = LoginInfo(id, smsCode.mobilePhoneNumber)
    if (smsCode.mobilePhoneNumber.startsWith("666") && smsCode.code == "666666") {
      // TODO This is a developer backdoor
      println("!!!")
      Future.successful(loginInfo)
    } else {
      AVOSUtils.verifySmsCode(smsCode.mobilePhoneNumber, smsCode.code).map {
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