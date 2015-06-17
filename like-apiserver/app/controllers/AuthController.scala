package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.IDGenerator
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfileBuilder, OAuth2Info }
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import extensions._
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.i18n.{ MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import services.UserService
import utils.{ MemcachedCacheClient, HashUtils, GenerateUtils }
import models.User

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/22/15
 */
class AuthController @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService,
    sessionTokenGenerator: IDGenerator,
    providerEnv: ProviderEnv,
    mobileProvider: MobileProvider) extends BaseController {

  implicit val mobileCodeReads = (
    (__ \ 'phone).read[String] and
    (__ \ 'zone).read[String] and
    (__ \ 'code).read[String]
  )(SmsCode.apply _)

  val refreshTokenGenerator = new SecureRandomIDGenerator(15)

  val DefaultAvatar = Play.configuration.getString("default.avatar").get
  val DefaultCover = Play.configuration.getString("default.cover").get
  val TokenExpiry = Play.configuration.getInt("default.tokenExpiry").get

  /**
   * Get renewed session token
   */
  def refreshSessionToken(id: Long) = Action.async(parse.json) { implicit request =>
    (request.body \ "refresh_token").asOpt[String] match {
      case Some(refreshToken) => userService.findById(id).flatMap {
        case Some(user) =>
          if (user.refreshToken.isDefined && HashUtils.validate(refreshToken, user.refreshToken.get)) {
            for {
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String]("session_user:" + sessionToken, user.identify, TokenExpiry)
              refreshToken <- refreshTokenGenerator.generate
              unit <- userService.updateRefreshToken(id, HashUtils.hashPassword(refreshToken))
            } yield {
              success(Messages("success.refreshToken"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken
              ))
            }
          } else {
            Future.successful(error(4013, Messages("invalid.sessionToken")))
          }
        case None => Future.successful(error(4014, Messages("invalid.userId")))
      }
      case None => Future.successful(error(4013, Messages("invalid.sessionToken")))
    }
  }

  /**
   * Authenticate via mobile sms code
   */
  def mobileAuthenticate = Action.async(parse.json) { implicit request =>
    request.body.validate[SmsCode].fold(
      errors => {
        Future.successful(error(4012, Messages("invalid.mobileCode")))
      },
      smsCode => mobileProvider.authenticate(smsCode).flatMap { loginInfo =>
        userService.findByMobileAndZone(smsCode.mobilePhoneNumber, smsCode.zone.toInt).flatMap {
          case Some(user) => // User already exists
            for {
              // Create new session token
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String]("session_user:" + sessionToken, user.identify, TokenExpiry)
              // Create new refresh token
              refreshToken <- refreshTokenGenerator.generate
              // Refresh user's refresh token and updated time
              unit <- userService.updateRefreshToken(user.id.get, HashUtils.hashPassword(refreshToken))
            } yield {
              success(Messages("success.login"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken,
                "expires_in" -> TokenExpiry
              ))
            }
          case None => // Register new user
            for {
              refreshToken <- refreshTokenGenerator.generate
              user <- userService.insert(User(None, Some(smsCode.mobilePhoneNumber), None,
                HashUtils.hashPassword(refreshToken),
                GenerateUtils.generateNickname(),
                DefaultAvatar, DefaultCover,
                GenerateUtils.currentSeconds(),
                GenerateUtils.currentSeconds(),
                0, Some(HashUtils.hashPassword(refreshToken))
              ))
              link <- userService.linkAccount(user.id.get, MobileProvider.ID, smsCode.zone.toInt + " " + smsCode.mobilePhoneNumber)
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String]("session_user:" + sessionToken, user.identify, TokenExpiry)
            } yield {
              success(Messages("success.login"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken,
                "expires_in" -> TokenExpiry
              )
              )
            }
        }
      }.recover {
        case e: Exception =>
          Logger.error(e.getMessage)
          error(4012, Messages("invalid.mobileCode"))
      }
    )
  }

  /**
   * Authenticate via social provider
   */
  def socialAuthenticate(provider: String) = Action.async(parse.json) { implicit request =>
    val idOpt = (request.body \ "uid").asOpt[String]
    val accessTokenOpt = (request.body \ "access_token").asOpt[String]

    (idOpt, accessTokenOpt) match {
      case (Some(id), Some(accessToken)) =>
        Logger.debug(s"[$provider]$id:$accessToken")
        val authInfo = OAuth2Info(accessToken)
        val params = authInfo.params.getOrElse(Map[String, String]()) + ("uid" -> id)
        val authInfoWithId = authInfo.copy(params = Some(params))

        providerEnv.providers.get(provider) match {
          case Some(p: WeiboProvider with WeiboProfileBuilder) =>
            for {
              profile <- p.retrieveProfile(authInfoWithId)
              refreshToken <- refreshTokenGenerator.generate
              user <- userService.upsert(profile.loginInfo, User(None, None, None,
                HashUtils.hashPassword(refreshToken),
                profile.screenName, DefaultAvatar, DefaultCover,
                GenerateUtils.currentSeconds(),
                GenerateUtils.currentSeconds(), 0, Some(HashUtils.hashPassword(refreshToken))))
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String]("session_user:" + sessionToken, user.identify, TokenExpiry)
            } yield {
              success(Messages("success.login"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken,
                "expires_in" -> TokenExpiry
              ))
            }
          case Some(p: WechatProvider with WechatProfileBuilder) =>
            for {
              profile <- p.retrieveProfile(authInfoWithId)
              refreshToken <- refreshTokenGenerator.generate
              user <- userService.upsert(profile.loginInfo, User(None, None, None,
                HashUtils.hashPassword(refreshToken),
                profile.screenName, DefaultAvatar, DefaultCover,
                GenerateUtils.currentSeconds(),
                GenerateUtils.currentSeconds(), 0, Some(HashUtils.hashPassword(refreshToken))))
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String]("session_user:" + sessionToken, user.identify, TokenExpiry)
            } yield {
              success(Messages("success.login"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken,
                "expires_in" -> TokenExpiry
              ))
            }
          case Some(p: FacebookProvider with CommonSocialProfileBuilder) =>
            for {
              profile <- p.retrieveProfile(authInfoWithId)
              refreshToken <- refreshTokenGenerator.generate
              user <- userService.upsert(profile.loginInfo, User(None, None, None,
                HashUtils.hashPassword(refreshToken),
                profile.fullName.getOrElse("New Liker"), DefaultAvatar, DefaultCover,
                GenerateUtils.currentSeconds(),
                GenerateUtils.currentSeconds(), 0, Some(HashUtils.hashPassword(refreshToken))))
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String]("session_user:" + sessionToken, user.identify, TokenExpiry)
            } yield {
              success(Messages("success.login"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken,
                "expires_in" -> TokenExpiry
              ))
            }
          case _ => Future.successful(error(4052, Messages("invalid.socialProvider")))
        }
      case _ => Future.successful(error(5051, Messages("invalid.authInfo")))
    }
  }

  def linkSocialAccount(provider: String) = SecuredAction.async(parse.json) { implicit request =>
    val idOpt = (request.body \ "uid").asOpt[String]
    val accessTokenOpt = (request.body \ "access_token").asOpt[String]

    (idOpt, accessTokenOpt) match {
      case (Some(id), Some(accessToken)) =>
        Logger.debug(s"[$provider]$id:$accessToken")
        val authInfo = OAuth2Info(accessToken)
        val params = authInfo.params.getOrElse(Map[String, String]()) + ("uid" -> id)
        val authInfoWithId = authInfo.copy(params = Some(params))

        userService.findBySocial(provider, id).flatMap {
          case Some(social) =>
            if (social.userId == request.userId) {
              Future.successful(error(4053, Messages("failed.linkByYou")))
            } else {
              Future.successful(error(4054, Messages("failed.linkByOthers")))
            }
          case None =>
            val response = success(Messages("success.link"), Json.obj(
              "user_id" -> request.userId.toString,
              "provider_id" -> provider,
              "provider_key" -> id
            ))
            providerEnv.providers.get(provider) match {
              case Some(p: WeiboProvider with WeiboProfileBuilder) =>
                for {
                  profile <- p.retrieveProfile(authInfoWithId)
                  link <- userService.linkAccount(request.userId, provider, profile.userId)
                } yield {
                  response
                }
              case Some(p: WechatProvider with WechatProfileBuilder) =>
                for {
                  profile <- p.retrieveProfile(authInfoWithId)
                  link <- userService.linkAccount(request.userId, provider, profile.userId)
                } yield {
                  response
                }
              case Some(p: FacebookProvider with CommonSocialProfileBuilder) =>
                for {
                  profile <- p.retrieveProfile(authInfoWithId)
                  link <- userService.linkAccount(request.userId, provider, profile.loginInfo.providerKey)
                } yield {
                  response
                }
              case _ => Future.successful(error(4052, Messages("invalid.socialProvider")))
            }
        }
      case _ => Future.successful(error(5051, Messages("invalid.authInfo")))
    }
  }

  def unlinkSocialAccount(provider: String) = SecuredAction.async(parse.json) { implicit request =>
    userService.unlinkAccount(request.userId, provider).map { _ =>
      success(Messages("success.unlink"))
    }
  }

  def linkMobileAccount = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[SmsCode].fold(
      errors => {
        Future.successful(error(4012, Messages("invalid.mobileCode")))
      },
      smsCode => userService.findByMobileAndZone(smsCode.mobilePhoneNumber, smsCode.zone.toInt).flatMap {
        case Some(user) => Future.successful(error(4011, Messages("mobile.exists")))
        case None =>
          mobileProvider.authenticate(smsCode).flatMap { loginInfo =>
            for {
              link <- userService.updateMobile(request.userId, smsCode.mobilePhoneNumber, smsCode.zone.toInt)
            } yield {
              success(Messages("success.link"), Json.obj(
                "user_id" -> request.userId.toString,
                "provider_id" -> MobileProvider.ID,
                "provider_key" -> (smsCode.zone.toInt + " " + smsCode.mobilePhoneNumber)
              ))
            }
          }.recover {
            case e: Exception =>
              Logger.error(e.getMessage)
              error(4012, Messages("invalid.mobileCode"))
          }
      }
    )
  }

}
