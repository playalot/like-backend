package modules

import com.google.inject.{ Provides, AbstractModule }
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.EventBus
import com.mohiva.play.silhouette.impl.authenticators.{ BearerTokenAuthenticatorService, BearerTokenAuthenticatorSettings, BearerTokenAuthenticator }
import com.mohiva.play.silhouette.impl.daos.CacheAuthenticatorDAO
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{ CookieStateSettings, CookieStateProvider }
import com.mohiva.play.silhouette.impl.providers.{ OAuth2StateProvider, OAuth2Settings }
import extensions.{ WechatProvider, WeiboProvider, ProviderEnv, MobileProvider }
import net.codingwell.scalaguice.ScalaModule

import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.api.util._
import services._

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    bind[PostService].to[PostServiceImpl]
    bind[TagService].to[TagServiceImpl]
    bind[UserService].to[UserServiceImpl]
    bind[MarkService].to[MarkServiceImpl]
    bind[NotificationService].to[NotificationServiceImpl]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator(32))
    bind[HTTPLayer].toInstance(new PlayHTTPLayer)
    bind[EventBus].toInstance(EventBus())
  }

  /**
   * Provides the provider environment.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    weiboProvider: WeiboProvider,
    wechatProvider: WechatProvider,
    facebookProvider: FacebookProvider): ProviderEnv = {
    ProviderEnv(
      Map(
        weiboProvider.id -> weiboProvider,
        wechatProvider.id -> wechatProvider,
        facebookProvider.id -> facebookProvider
      )
    )
  }

  /**
   * Provides the authenticator service.
   *
   * @param idGenerator The ID generator used to create the authenticator ID.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
    cacheLayer: CacheLayer,
    idGenerator: IDGenerator): AuthenticatorService[BearerTokenAuthenticator] = {

    new BearerTokenAuthenticatorService(BearerTokenAuthenticatorSettings(
      headerName = Play.configuration.getString("silhouette.authenticator.headerName").get,
      authenticatorIdleTimeout = Play.configuration.getInt("silhouette.authenticator.authenticatorIdleTimeout"),
      authenticatorExpiry = Play.configuration.getInt("silhouette.authenticator.authenticatorExpiry").get
    ), new CacheAuthenticatorDAO[BearerTokenAuthenticator](cacheLayer), idGenerator, Clock())
  }

  /**
   * Provides the OAuth2 state provider.
   *
   * @param idGenerator The ID generator implementation.
   * @return The OAuth2 state provider implementation.
   */
  @Provides
  def provideOAuth2StateProvider(idGenerator: IDGenerator): OAuth2StateProvider = {
    new CookieStateProvider(CookieStateSettings(
      cookieName = Play.configuration.getString("silhouette.oauth2StateProvider.cookieName").get,
      cookiePath = Play.configuration.getString("silhouette.oauth2StateProvider.cookiePath").get,
      cookieDomain = Play.configuration.getString("silhouette.oauth2StateProvider.cookieDomain"),
      secureCookie = Play.configuration.getBoolean("silhouette.oauth2StateProvider.secureCookie").get,
      httpOnlyCookie = Play.configuration.getBoolean("silhouette.oauth2StateProvider.httpOnlyCookie").get,
      expirationTime = Play.configuration.getInt("silhouette.oauth2StateProvider.expirationTime").get
    ), idGenerator, Clock())
  }

  /**
   * Provides the mobile provider.
   *
   * @return The mobile provider.
   */
  @Provides
  def provideMobileProvider(): MobileProvider = {
    new MobileProvider()
  }

  /**
   * Provides the Weibo provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param stateProvider The OAuth2 state provider implementation.
   * @return The Weibo provider.
   */
  @Provides
  def provideWeiboProvider(httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider): WeiboProvider = {
    new WeiboProvider(httpLayer, stateProvider, OAuth2Settings(
      authorizationURL = Play.configuration.getString("silhouette.weibo.authorizationURL"),
      accessTokenURL = Play.configuration.getString("silhouette.weibo.accessTokenURL").get,
      redirectURL = Play.configuration.getString("silhouette.weibo.redirectURL").get,
      clientID = Play.configuration.getString("silhouette.weibo.clientID").getOrElse(""),
      clientSecret = Play.configuration.getString("silhouette.weibo.clientSecret").getOrElse(""),
      scope = Play.configuration.getString("silhouette.weibo.scope")))
  }

  /**
   * Provides the Wechat provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param stateProvider The OAuth2 state provider implementation.
   * @return The Wechat provider.
   */
  @Provides
  def provideWechatProvider(httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider): WechatProvider = {
    new WechatProvider(httpLayer, stateProvider, OAuth2Settings(
      authorizationURL = Play.configuration.getString("silhouette.wechat.authorizationURL"),
      accessTokenURL = Play.configuration.getString("silhouette.wechat.accessTokenURL").get,
      redirectURL = Play.configuration.getString("silhouette.wechat.redirectURL").get,
      clientID = Play.configuration.getString("silhouette.wechat.clientID").getOrElse(""),
      clientSecret = Play.configuration.getString("silhouette.wechat.clientSecret").getOrElse(""),
      scope = Play.configuration.getString("silhouette.wechat.scope")))
  }

  /**
   * Provides the Facebook provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param stateProvider The OAuth2 state provider implementation.
   * @return The Facebook provider.
   */
  @Provides
  def provideFacebookProvider(httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider): FacebookProvider = {
    new FacebookProvider(httpLayer, stateProvider, OAuth2Settings(
      authorizationURL = Play.configuration.getString("silhouette.facebook.authorizationURL"),
      accessTokenURL = Play.configuration.getString("silhouette.facebook.accessTokenURL").get,
      redirectURL = Play.configuration.getString("silhouette.facebook.redirectURL").get,
      clientID = Play.configuration.getString("silhouette.facebook.clientID").getOrElse(""),
      clientSecret = Play.configuration.getString("silhouette.facebook.clientSecret").getOrElse(""),
      scope = Play.configuration.getString("silhouette.facebook.scope")))
  }

}
