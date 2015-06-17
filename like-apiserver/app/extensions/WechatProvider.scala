package extensions

/**
 * Created by Guan Guan
 * Date: 5/29/15
 */
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.{ UnexpectedResponseException, ProfileRetrievalException }
import com.mohiva.play.silhouette.impl.providers.OAuth2Provider._
import com.mohiva.play.silhouette.impl.providers._
import play.api.libs.json.{ Json, JsObject, JsValue }
import play.api.libs.ws.WSResponse

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }
import extensions.WechatProvider._

/**
 * Base Wechat OAuth2 Provider.
 */
trait BaseWechatProvider extends OAuth2Provider {

  /**
   * The content type to parse a profile from.
   */
  override type Content = JsValue

  /**
   * The provider ID.
   */
  override val id = ID

  /**
   * Defines the URLs that are needed to retrieve the profile data.
   */
  override protected val urls = Map("api" -> API)

  /**
   * Builds the social profile.
   *
   * @param authInfo The auth info received from the provider.
   * @return On success the build social profile, otherwise a failure.
   */
  override protected def buildProfile(authInfo: OAuth2Info): Future[Profile] = {
    val uid = authInfo.params.map(_.getOrElse("uid", "")).getOrElse("")
    httpLayer.url(urls("api").format(uid, authInfo.accessToken)).get().flatMap { response =>
      val json = response.json

      (json \ "error").asOpt[JsObject] match {
        case Some(error) =>
          val errorMsg = (error \ "message").as[String]
          val errorType = (error \ "type").as[String]
          val errorCode = (error \ "code").as[Int]

          throw new ProfileRetrievalException(SpecifiedProfileError.format(id, errorMsg, errorType, errorCode))
        case _ => profileParser.parse(json)
      }
    }
  }

  /**
   * Builds the OAuth2 info.
   *
   * Wechat does not follow the OAuth2 spec :-\
   *
   * @param response The response from the provider.
   * @return The OAuth2 info on success, otherwise an failure.
   */
  override protected def buildInfo(response: WSResponse): Try[OAuth2Info] = {
    try {
      val json = Json.parse(response.body)
      val token = (json \ "access_token").as[String]
      val uid = (json \ "openid").as[String]
      val expiresIn = (json \ "expires_in").as[Int]
      Success(OAuth2Info(token, None, Some(expiresIn), params = Some(Map("uid" -> uid))))
    } catch {
      case _: Throwable => Failure(new UnexpectedResponseException(InvalidInfoFormat.format(id, response.body)))
    }
  }
}

case class WechatProfile(
  loginInfo: LoginInfo,
  userId: String,
  screenName: String,
  biography: Option[String] = None,
  location: Option[String] = None,
  gender: Option[String] = None,
  avatarUrl: Option[String] = None,
  email: Option[String] = None) extends SocialProfile

trait WechatProfileBuilder extends SocialProfileBuilder {
  self: WechatProvider =>

  /**
   * The type of the profile a profile builder is responsible for.
   */
  type Profile = WechatProfile

  val profileParser = new WechatProfileParser
}

/**
 * The profile parser for the common social profile.
 */
class WechatProfileParser extends SocialProfileParser[JsValue, WechatProfile] {

  /**
   * Parses the social profile.
   *
   * @param json The content returned from the provider.
   * @return The social profile from given result.
   */
  override def parse(json: JsValue) = Future.successful {
    val userId = (json \ "openid").as[String]
    val screenName = (json \ "nickname").as[String]
    val biography = (json \ "description").asOpt[String]
    val location = (json \ "city").asOpt[String]
    val gender = (json \ "sex").asOpt[String]
    val avatarUrl = (json \ "headimgurl").asOpt[String]

    WechatProfile(
      userId = userId,
      loginInfo = LoginInfo(ID, userId),
      screenName = screenName,
      biography = biography,
      location = location,
      gender = gender,
      avatarUrl = avatarUrl,
      email = None
    )
  }

  trait WechatProfileBuilder extends SocialProfileBuilder {
    self: WechatProvider =>

    /**
     * The type of the profile a profile builder is responsible for.
     */
    type Profile = WechatProfile

    val profileParser = new WechatProfileParser
  }

}

/**
 * The Wechat OAuth2 Provider.
 *
 * @param httpLayer The HTTP layer implementation.
 * @param stateProvider The state provider implementation.
 * @param settings The provider settings.
 */
class WechatProvider(
  protected val httpLayer: HTTPLayer,
  protected val stateProvider: OAuth2StateProvider,
  val settings: OAuth2Settings)
    extends BaseWechatProvider with WechatProfileBuilder {

  /**
   * The type of this class.
   */
  override type Self = WechatProvider

  /**
   * The profile parser implementation.
   */
  override val profileParser = new WechatProfileParser

  /**
   * Gets a provider initialized with a new settings object.
   *
   * @param f A function which gets the settings passed and returns different settings.
   * @return An instance of the provider initialized with new settings.
   */
  override def withSettings(f: (Settings) => Settings) = new WechatProvider(httpLayer, stateProvider, f(settings))
}

/**
 * The companion object.
 */
object WechatProvider {

  /**
   * The error messages.
   */
  val SpecifiedProfileError = "[Silhouette][%s] Error retrieving profile information. Error message: %s, type: %s, code: %s"

  /**
   * The Wechat constants.
   */
  val ID = "wechat"
  val API = "https://api.weixin.qq.com/sns/userinfo?openid=%s&access_token=%s"
}