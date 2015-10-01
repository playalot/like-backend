package modules

import actors.ClassificationActor
import com.google.inject.{ Provides, AbstractModule }
import com.likeorz.actors._
import com.likeorz.silhouettes.MobileProvider
import com.mohiva.play.silhouette.api.EventBus
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import com.mohiva.play.silhouette.impl.providers.oauth2.state.{ CookieStateProvider, CookieStateSettings }
import com.mohiva.play.silhouette.impl.providers.{ OAuth2StateProvider, OAuth2Settings }
import extensions.{ WechatProvider, WeiboProvider, ProviderEnv }
import net.codingwell.scalaguice.ScalaModule
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._

import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.api.util._

import play.api.Play
import play.api.Play.current
import play.api.Configuration
import play.api.libs.ws.WSClient
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.concurrent.AkkaGuiceSupport
import services.{ OnStartServiceImpl, OnStartService }

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    // Services start on application start
    bind[OnStartService].to[OnStartServiceImpl].asEagerSingleton()

    // SilhouetteModule
    bind[CacheLayer].to[PlayCacheLayer]
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator(32))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())
    bindActor[ClassificationActor]("classification-actor")
    bindActor[PushNewLikesActor]("push-likes-actor")
  }

  /**
   * Provides the HTTP layer implementation.
   *
   * @param client Play's WS client.
   * @return The HTTP layer implementation.
   */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

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
   * Provides the OAuth2 state provider.
   *
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The OAuth2 state provider implementation.
   */
  @Provides
  def provideOAuth2StateProvider(idGenerator: IDGenerator, configuration: Configuration, clock: Clock): OAuth2StateProvider = {
    val settings = configuration.underlying.as[CookieStateSettings]("silhouette.oauth2StateProvider")
    new CookieStateProvider(settings, idGenerator, clock)
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
