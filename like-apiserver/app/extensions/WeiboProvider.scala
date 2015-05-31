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
import extensions.WeiboProvider._

/**
 * Base Weibo OAuth2 Provider.
 */
trait BaseWeiboProvider extends OAuth2Provider {

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
      println(json)
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
   * Weibo does not follow the OAuth2 spec :-\
   *
   * @param response The response from the provider.
   * @return The OAuth2 info on success, otherwise an failure.
   */
  override protected def buildInfo(response: WSResponse): Try[OAuth2Info] = {
    try {
      val json = Json.parse(response.body)
      val token = (json \ "access_token").as[String]
      val uid = (json \ "uid").as[String]
      val expiresIn = (json \ "expires_in").as[Int]
      Success(OAuth2Info(token, None, Some(expiresIn), params = Some(Map("uid" -> uid))))
    } catch {
      case _: Throwable => Failure(new UnexpectedResponseException(InvalidInfoFormat.format(id, response.body)))
    }
  }
}

case class WeiboProfile(
  loginInfo: LoginInfo,
  userId: String,
  screenName: String,
  biography: Option[String] = None,
  location: Option[String] = None,
  gender: Option[String] = None,
  avatarUrl: Option[String] = None,
  email: Option[String] = None) extends SocialProfile

trait WeiboProfileBuilder extends SocialProfileBuilder {
  self: WeiboProvider =>

  /**
   * The type of the profile a profile builder is responsible for.
   */
  type Profile = WeiboProfile

  val profileParser = new WeiboProfileParser
}

/**
 * The profile parser for the common social profile.
 */
class WeiboProfileParser extends SocialProfileParser[JsValue, WeiboProfile] {

  /**
   * Parses the social profile.
   *
   * @param json The content returned from the provider.
   * @return The social profile from given result.
   */
  override def parse(json: JsValue) = Future.successful {
    val userId = (json \ "id").as[Long].toString
    val screenName = (json \ "screen_name").as[String]
    val biography = (json \ "description").asOpt[String]
    val location = (json \ "location").asOpt[String]
    val gender = (json \ "gender").asOpt[String]
    val avatarUrl = (json \ "profile_image_url").asOpt[String]

    WeiboProfile(
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

  trait WeiboProfileBuilder extends SocialProfileBuilder {
    self: WeiboProvider =>

    /**
     * The type of the profile a profile builder is responsible for.
     */
    type Profile = WeiboProfile

    val profileParser = new WeiboProfileParser
  }

}

/**
 * The Weibo OAuth2 Provider.
 *
 * @param httpLayer The HTTP layer implementation.
 * @param stateProvider The state provider implementation.
 * @param settings The provider settings.
 */
class WeiboProvider(
  protected val httpLayer: HTTPLayer,
  protected val stateProvider: OAuth2StateProvider,
  val settings: OAuth2Settings)
    extends BaseWeiboProvider with WeiboProfileBuilder {

  /**
   * The type of this class.
   */
  override type Self = WeiboProvider

  /**
   * The profile parser implementation.
   */
  override val profileParser = new WeiboProfileParser

  /**
   * Gets a provider initialized with a new settings object.
   *
   * @param f A function which gets the settings passed and returns different settings.
   * @return An instance of the provider initialized with new settings.
   */
  override def withSettings(f: (Settings) => Settings) = new WeiboProvider(httpLayer, stateProvider, f(settings))
}

/**
 * The companion object.
 */
object WeiboProvider {

  /**
   * The error messages.
   */
  val SpecifiedProfileError = "[Silhouette][%s] Error retrieving profile information. Error message: %s, type: %s, code: %s"

  /**
   * The Weibo constants.
   */
  val ID = "weibo"
  val API = "https://api.weibo.com/2/users/show.json?uid=%s&access_token=%s"
}