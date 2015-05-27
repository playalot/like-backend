package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ LoginInfo, LoginEvent, Environment, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import extensions.{ MobileProvider, SmsCode }
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.i18n.{ Lang, MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
import services.UserService
import utils.{ MemcachedCacheClient, HashUtils, QiniuUtil, GenerateUtils }
import models.User

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/22/15
 */
class AuthController @Inject() (
    val messagesApi: MessagesApi,
    userService: UserService,
    implicit val env: Environment[User, BearerTokenAuthenticator],
    mobileProvider: MobileProvider) extends BaseController {

  implicit val mobileCodeReads = (
    (__ \ 'mobile).read[String] and
    (__ \ 'code).read[String]
  )(SmsCode.apply _)

  val tokenGenerator = new SecureRandomIDGenerator(15)

  val DefaultAvatar = Play.configuration.getString("default.avatar").get
  val DefaultCover = Play.configuration.getString("default.cover").get

  val TokenExpiry = Play.configuration.getInt("default.tokenExpiry").get

  /**
   * Get Qiniu upload token
   */
  def qiniuUploadToken = SecuredAction {
    Ok(Json.obj(
      "code" -> 1,
      "message" -> "Get Upload Token Sucess",
      "data" -> Json.obj(
        "upload_token" -> QiniuUtil.getUploadToken()
      )
    ))
  }

  /**
   * Get renewed session token
   * @param id ID of the user
   */
  def refreshSessionToken(id: Long) = Action.async(parse.json) { implicit request =>
    userService.findById(id).flatMap {
      case Some(user) =>
        val refreshToken = (request.body \ "refresh_token").as[String]
        if (user.refreshToken.isDefined && HashUtils.validate(refreshToken, user.refreshToken.get)) {
          val loginInfo = LoginInfo(MobileProvider.ID, user.mobile)
          for {
            authenticator <- env.authenticatorService.create(loginInfo)
            newToken <- tokenGenerator.generate
            value <- MemcachedCacheClient.saveAsync[Long](authenticator.id, user.id.get, TokenExpiry)
            //env.authenticatorService.init(authenticator)
            unit <- userService.updateRefreshToken(user.id.get, HashUtils.hashPassword(newToken))
          } yield {
            Ok(Json.obj(
              "code" -> 1,
              "message" -> "Session Refresh Success",
              "data" -> Json.obj(
                "user_id" -> user.id.get.toString,
                "session_token" -> authenticator.id,
                "refresh_token" -> newToken
              )
            ))
          }
        } else {
          Future.successful(Unauthorized(Json.obj(
            "code" -> 4013,
            "field" -> "refresh_token",
            "message" -> "Invalid Refresh Token"
          )))
        }
      case None =>
        Future.successful(NotFound(Json.obj(
          "code" -> 4014,
          "field" -> "id",
          "message" -> "Invalid User ID"
        )))
    }

  }

  /**
   * Authenticate via mobile sms code
   */
  def mobileAuthenticate = Action.async(parse.json) { implicit request =>
    request.body.validate[SmsCode].fold(
      errors => {
        Future.successful(Unauthorized(Json.obj(
          "code" -> 4012,
          "field" -> "code",
          "message" -> Messages("invalid.mobileCode")
        )))
      },
      smsCode => mobileProvider.authenticate(smsCode).flatMap { loginInfo =>
        env.authenticatorService.create(loginInfo).flatMap { authenticator =>
          userService.findByMobile(loginInfo.providerKey).flatMap {
            case Some(user) => // User already exists
              for {
                // Create new session token
                value <- MemcachedCacheClient.saveAsync[Long](authenticator.id, user.id.get, TokenExpiry)
                //env.authenticatorService.init(authenticator)
                // Create new refresh token
                newToken <- tokenGenerator.generate
                // Refresh user's refresh token and updated time
                unit <- userService.updateRefreshToken(user.id.get, HashUtils.hashPassword(newToken))
              } yield {
                env.eventBus.publish(LoginEvent(user, request, request2Messages))
                Ok(Json.obj(
                  "code" -> 1,
                  "message" -> "Login Success",
                  "data" -> Json.obj(
                    "user_id" -> user.id.get.toString,
                    "session_token" -> authenticator.id,
                    "refresh_token" -> newToken,
                    "expires_in" -> (DateTime.now to authenticator.expirationDate).millis / 1000
                  )
                ))
              }
            case None => // Register new user
              for {
                refreshToken <- tokenGenerator.generate
                user <- userService.insert(User(
                  None,
                  smsCode.mobilePhoneNumber,
                  None,
                  HashUtils.hashPassword(refreshToken),
                  GenerateUtils.generateNickname(),
                  DefaultAvatar,
                  DefaultCover,
                  GenerateUtils.currentSeconds,
                  GenerateUtils.currentSeconds,
                  0,
                  Some(HashUtils.hashPassword(refreshToken))
                ))
                value <- MemcachedCacheClient.saveAsync[Long](authenticator.id, user.id.get, TokenExpiry)
              } yield {
                env.eventBus.publish(LoginEvent(user, request, request2Messages))
                Ok(Json.obj(
                  "code" -> 1,
                  "message" -> "Login Success",
                  "data" -> Json.obj(
                    "user_id" -> user.id.get.toString,
                    "session_token" -> authenticator.id,
                    "refresh_token" -> refreshToken,
                    "expires_in" -> (DateTime.now to authenticator.expirationDate).millis / 1000
                  )
                ))
              }
          }
        }
      }.recover {
        case e: Exception =>
          Logger.error(e.getMessage)
          Unauthorized(Json.obj(
            "code" -> 4012,
            "field" -> "code",
            "message" -> Messages("invalid.mobileCode")
          ))
      }
    )
  }

}
