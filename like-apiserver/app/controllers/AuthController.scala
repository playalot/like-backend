package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ LoginInfo, LoginEvent, Environment }
import com.mohiva.play.silhouette.impl.authenticators.BearerTokenAuthenticator
import com.mohiva.play.silhouette.impl.providers.{ CommonSocialProfileBuilder, CommonSocialProfile, SocialProvider, OAuth2Info }
import com.mohiva.play.silhouette.impl.util.SecureRandomIDGenerator
import extensions._
import play.api.{ Logger, Play }
import play.api.Play.current
import play.api.i18n.{ MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.github.nscala_time.time.Imports._
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
    implicit val env: Environment[User, BearerTokenAuthenticator],
    providerEnv: ProviderEnv,
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
            value <- MemcachedCacheClient.saveAsync[String]("session_user:" + authenticator.id, user.id.get.toString, TokenExpiry)
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
                value <- MemcachedCacheClient.saveAsync[String]("session_user:" + authenticator.id, user.id.get.toString, TokenExpiry)
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
                value <- MemcachedCacheClient.saveAsync[String]("session_user:" + authenticator.id, user.id.get.toString, TokenExpiry)
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

  def socialAuthenticate(provider: String) = Action.async { implicit request =>

    //    val idOpt = (request.body \ "uid").asOpt[String]
    //    val accessTokenOpt = (request.body \ "access_token").asOpt[String]
    val idOpt = Some("1")
    val accessTokenOpt = Some("2")

    (idOpt, accessTokenOpt) match {
      case (Some(id), Some(accessToken)) =>
        println("[]" + id + accessToken)
        //        val authInfo = OAuth2Info(accessToken)
        //        val params = authInfo.params.getOrElse(Map[String, String]()) + ("uid" -> id)
        //        val authInfoWithId = authInfo.copy(params = Some(params))
        providerEnv.providers.get(provider) match {
          case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
            println("!!!!" + p.id)
            p.authenticate().flatMap { r =>
              println(r); r match {
                case Left(result) =>
                  Future.successful(result)
                case Right(authInfo) =>
                  for {
                    profile <- p.retrieveProfile(authInfo)
                    //                user <- userService.save(profile)
                    //                authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
                    //                authenticator <- env.authenticatorService.create(profile.loginInfo)
                    //                value <- env.authenticatorService.init(authenticator)
                    //                result <- env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index()))
                  } yield {
                    //                  env.eventBus.publish(LoginEvent(user, request, request2Messages))
                    //                  result
                    println(profile)
                    Ok
                  }
              }
            }
          case Some(p: WeiboProvider with WeiboProfileBuilder) =>
            println("####" + p.id)
            p.authenticate().flatMap { r =>
              println(r); r match {
                case Left(result) =>
                  Future.successful(result)
                case Right(authInfo) =>
                  for {
                    profile <- p.retrieveProfile(authInfo)
                    //                user <- userService.save(profile)
                    //                authInfo <- authInfoRepository.save(profile.loginInfo, authInfo)
                    //                authenticator <- env.authenticatorService.create(profile.loginInfo)
                    //                value <- env.authenticatorService.init(authenticator)
                    //                result <- env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index()))
                  } yield {
                    //                  env.eventBus.publish(LoginEvent(user, request, request2Messages))
                    //                  result
                    println(profile)
                    Ok
                  }
              }
            }
          case _ =>
            println("====")
            Future.successful(Ok(Json.obj(
              "code" -> 4052,
              "message" -> Messages("invalid.socialProvider")
            )))
        }
      case _ => Future.successful(Ok(Json.obj(
        "code" -> 5051,
        "message" -> Messages("invalid.authInfo")
      )))
    }

  }

}
