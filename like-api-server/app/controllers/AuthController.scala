package controllers

import javax.inject.Inject

import com.likeorz.services.UserService
import com.likeorz.silhouettes.{ SmsCode, MobileProvider }
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.IDGenerator
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfileBuilder, OAuth2Info }
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import extensions._
import com.likeorz.models._
import com.likeorz.utils.{ AVOSUtils, GenerateUtils, KeyUtils }
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.i18n.{ MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import utils._

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
    (__ \ 'mobile).read[String] and
    (__ \ 'zone).read[String] and
    (__ \ 'code).read[String]
  )(SmsCode.apply _)

  val refreshTokenGenerator = new SecureRandomIDGenerator(15)

  val DEFAULT_AVATAR = Play.configuration.getString("default.avatar").get
  val DEFAULT_COVER = Play.configuration.getString("default.cover").get
  val TOKEN_EXPIRY = Play.configuration.getInt("default.tokenExpiry").get

  /** Get renewed session token */
  def refreshSessionToken(id: Long) = Action.async(parse.json) { implicit request =>
    (request.body \ "refresh_token").asOpt[String] match {
      case Some(refreshToken) => userService.findById(id).flatMap {
        case Some(user) =>
          if (user.refreshToken.isDefined && HashUtils.validate(refreshToken, user.refreshToken.get)) {
            for {
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String](KeyUtils.session(sessionToken), user.identify, TOKEN_EXPIRY)
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
            Future.successful(error(4013, Messages("invalid.refreshToken")))
          }
        case None => Future.successful(error(4014, Messages("invalid.userId")))
      }
      case None => Future.successful(error(4013, Messages("invalid.refreshToken")))
    }
  }

  /** Send sms code to mobile phone */
  def sendSmsCode = Action.async(parse.json) { implicit request =>
    val tokenOpt = (request.body \ "token").asOpt[String]
    val mobileOpt = (request.body \ "mobile").asOpt[String]
    val zoneOpt = (request.body \ "zone").asOpt[String]
    (tokenOpt, zoneOpt, mobileOpt) match {
      case (Some(token), Some(zone), Some(mobile)) =>
        val valid = HashUtils.validateTimestampHash(token)
        if (valid) {
          AVOSUtils.sendSmsCode(mobile, zone).map {
            case true  => success(Messages("success.sendCode"))
            case false => error(4015, Messages("failed.sendCode"))
          }
        } else {
          Future.successful(error(4015, Messages("failed.sendCode")))
        }
      case _ => Future.successful(error(4015, Messages("failed.sendCode")))
    }
  }

  val countryMapping = Map(
    "CN" -> "86",
    "US" -> "1",
    "JP" -> "81",
    "YE" -> "967",
    "MO" -> "417",
    "HK" -> "852"
  )

  /** Authenticate via mobile sms code */
  def mobileAuthenticateViaMob = Action.async(parse.json) { implicit request =>
    request.body.validate[SmsCode].fold(
      errors => {
        Future.successful(error(4012, Messages("invalid.mobileCode")))
      },
      smsCodeRaw => {
        println(smsCodeRaw)
        val smsCode = smsCodeRaw.copy(zone = countryMapping.getOrElse(smsCodeRaw.zone, smsCodeRaw.zone))
        println(smsCode)
        mobileProvider.authenticateViaMob(smsCode).flatMap { loginInfo =>
          userService.findByMobileAndZone(smsCode.mobilePhoneNumber, smsCode.zone).flatMap {
            case Some(user) => // User already exists
              for {
                // Create new sekikl;p;l'ssion token
                sessionToken <- sessionTokenGenerator.generate
                value <- MemcachedCacheClient.saveAsync[String](KeyUtils.session(sessionToken), user.identify, TOKEN_EXPIRY)
                // Create new refresh token
                refreshToken <- refreshTokenGenerator.generate
                // Refresh user's refresh token and updated time
                unit <- userService.updateRefreshToken(user.id.get, HashUtils.hashPassword(refreshToken))
              } yield {
                success(Messages("success.login"), Json.obj(
                  "user_id" -> user.identify,
                  "session_token" -> sessionToken,
                  "refresh_token" -> refreshToken,
                  "expires_in" -> TOKEN_EXPIRY
                ))
              }
            case None => // Register new user
              for {
                refreshToken <- refreshTokenGenerator.generate
                user <- userService.insert(User(None, Some(smsCode.mobilePhoneNumber), None,
                  HashUtils.hashPassword(refreshToken),
                  GenerateUtils.generateNickname(),
                  DEFAULT_AVATAR, DEFAULT_COVER,
                  GenerateUtils.currentSeconds(),
                  GenerateUtils.currentSeconds(),
                  0, Some(HashUtils.hashPassword(refreshToken))
                ))
                link <- userService.linkAccount(user.id.get, MobileProvider.ID, smsCode.zone + " " + smsCode.mobilePhoneNumber)
                sessionToken <- sessionTokenGenerator.generate
                value <- MemcachedCacheClient.saveAsync[String](KeyUtils.session(sessionToken), user.identify, TOKEN_EXPIRY)
              } yield {
                success(Messages("success.login"), Json.obj(
                  "user_id" -> user.identify,
                  "session_token" -> sessionToken,
                  "refresh_token" -> refreshToken,
                  "expires_in" -> TOKEN_EXPIRY
                )
                )
              }
          }
        }
      }.recover {
        case e: Exception =>
          Logger.error(e.getMessage)
          error(4012, Messages("invalid.mobileCode"))
      }
    )
  }

  /** Authenticate via mobile sms code */
  def mobileAuthenticateViaAvos = Action.async(parse.json) { implicit request =>
    request.body.validate[SmsCode].fold(
      errors => {
        Future.successful(error(4012, Messages("invalid.mobileCode")))
      },
      smsCode => mobileProvider.authenticateViaAvos(smsCode).flatMap { loginInfo =>

        userService.findByMobileAndZone(smsCode.mobilePhoneNumber, smsCode.zone).flatMap {
          case Some(user) => // User already exists
            for {
              // Create new session token
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String](KeyUtils.session(sessionToken), user.identify, TOKEN_EXPIRY)
              // Create new refresh token
              refreshToken <- refreshTokenGenerator.generate
              // Refresh user's refresh token and updated time
              unit <- userService.updateRefreshToken(user.id.get, HashUtils.hashPassword(refreshToken))
            } yield {
              success(Messages("success.login"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken,
                "expires_in" -> TOKEN_EXPIRY
              ))
            }
          case None => // Register new user
            for {
              refreshToken <- refreshTokenGenerator.generate
              user <- userService.insert(User(None, Some(smsCode.mobilePhoneNumber), None,
                HashUtils.hashPassword(refreshToken),
                GenerateUtils.generateNickname(),
                DEFAULT_AVATAR, DEFAULT_COVER,
                GenerateUtils.currentSeconds(),
                GenerateUtils.currentSeconds(),
                0, Some(HashUtils.hashPassword(refreshToken))
              ))
              link <- userService.linkAccount(user.id.get, MobileProvider.ID, smsCode.zone + " " + smsCode.mobilePhoneNumber)
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String](KeyUtils.session(sessionToken), user.identify, TOKEN_EXPIRY)
            } yield {
              success(Messages("success.login"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken,
                "expires_in" -> TOKEN_EXPIRY
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

  /** Authenticate via social provider */
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
                profile.screenName, DEFAULT_AVATAR, DEFAULT_COVER,
                GenerateUtils.currentSeconds(),
                GenerateUtils.currentSeconds(), 0, Some(HashUtils.hashPassword(refreshToken))))
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String](KeyUtils.session(sessionToken), user.identify, TOKEN_EXPIRY)
            } yield {
              success(Messages("success.login"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken,
                "expires_in" -> TOKEN_EXPIRY
              ))
            }
          case Some(p: WechatProvider with WechatProfileBuilder) =>
            for {
              profile <- p.retrieveProfile(authInfoWithId)
              refreshToken <- refreshTokenGenerator.generate
              user <- userService.upsert(profile.loginInfo, User(None, None, None,
                HashUtils.hashPassword(refreshToken),
                profile.screenName, DEFAULT_AVATAR, DEFAULT_COVER,
                GenerateUtils.currentSeconds(),
                GenerateUtils.currentSeconds(), 0, Some(HashUtils.hashPassword(refreshToken))))
              sessionToken <- sessionTokenGenerator.generate
              value <- MemcachedCacheClient.saveAsync[String](KeyUtils.session(sessionToken), user.identify, TOKEN_EXPIRY)
            } yield {
              success(Messages("success.login"), Json.obj(
                "user_id" -> user.identify,
                "session_token" -> sessionToken,
                "refresh_token" -> refreshToken,
                "expires_in" -> TOKEN_EXPIRY
              ))
            }
          case Some(p: FacebookProvider with CommonSocialProfileBuilder) =>
            // Verify token
            if (HashUtils.validateTimestampHash((request.body \ "token").asOpt[String].getOrElse(""))) {
              val nickname = (request.body \ "nickname").asOpt[String].getOrElse("New Liker")
              for {
                refreshToken <- refreshTokenGenerator.generate
                user <- userService.upsert(LoginInfo(FacebookProvider.ID, id), User(None, None, None,
                  HashUtils.hashPassword(refreshToken),
                  nickname, DEFAULT_AVATAR, DEFAULT_COVER,
                  GenerateUtils.currentSeconds(),
                  GenerateUtils.currentSeconds(), 0, Some(HashUtils.hashPassword(refreshToken))))
                sessionToken <- sessionTokenGenerator.generate
                value <- MemcachedCacheClient.saveAsync[String](KeyUtils.session(sessionToken), user.identify, TOKEN_EXPIRY)
              } yield {
                success(Messages("success.login"), Json.obj(
                  "user_id" -> user.identify,
                  "session_token" -> sessionToken,
                  "refresh_token" -> refreshToken,
                  "expires_in" -> TOKEN_EXPIRY
                ))
              }
            } else {
              Future.successful(error(5051, Messages("invalid.authInfo")))
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
                // Verify token
                if (HashUtils.validateTimestampHash((request.body \ "token").asOpt[String].getOrElse(""))) {
                  for {
                    link <- userService.linkAccount(request.userId, provider, id)
                  } yield {
                    response
                  }
                } else {
                  Future.successful(error(5051, Messages("invalid.authInfo")))
                }
              case _ => Future.successful(error(4052, Messages("invalid.socialProvider")))
            }
        }
      case _ => Future.successful(error(5051, Messages("invalid.authInfo")))
    }
  }

  def unlinkSocialAccount(provider: String) = SecuredAction.async { implicit request =>
    userService.listLinkedAccounts(request.userId).flatMap { accounts =>
      if (accounts.size <= 1) {
        Future.successful(error(4055, Messages("failed.lastAccount")))
      } else {
        userService.unlinkAccount(request.userId, provider).map {
          case true  => success(Messages("success.unLink"))
          case false => error(4056, Messages("failed.unLink"))
        }
      }
    }
  }

  def linkMobileAccount = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[SmsCode].fold(
      errors => {
        Future.successful(error(4012, Messages("invalid.mobileCode")))
      },
      smsCode => userService.findByMobileAndZone(smsCode.mobilePhoneNumber, smsCode.zone).flatMap {
        case Some(user) => Future.successful(error(4011, Messages("invalid.mobileExists")))
        case None =>
          mobileProvider.authenticateViaAvos(smsCode).flatMap { loginInfo =>
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

  def linkMobileAccountViaMob = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[SmsCode].fold(
      errors => {
        Future.successful(error(4012, Messages("invalid.mobileCode")))
      },
      smsCode => userService.findByMobileAndZone(smsCode.mobilePhoneNumber, smsCode.zone).flatMap {
        case Some(user) => Future.successful(error(4011, Messages("invalid.mobileExists")))
        case None =>
          mobileProvider.authenticateViaMob(smsCode).flatMap { loginInfo =>
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

  /**
   * List all social accounts linked to this user
   */
  def getLinkedAccounts = SecuredAction.async { implicit request =>
    userService.listLinkedAccounts(request.userId).map { accounts =>
      success(Messages("success.found"), Json.obj("linked_accounts" -> Json.toJson(accounts)))
    }
  }
}
