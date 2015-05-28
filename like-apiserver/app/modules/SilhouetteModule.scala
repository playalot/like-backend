package modules

import com.google.inject.{ Provides, AbstractModule }
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.{ Environment, EventBus }
import com.mohiva.play.silhouette.impl.authenticators.{ BearerTokenAuthenticatorService, BearerTokenAuthenticatorSettings, BearerTokenAuthenticator }
import com.mohiva.play.silhouette.impl.daos.CacheAuthenticatorDAO
import extensions.MobileProvider
import net.codingwell.scalaguice.ScalaModule

import com.mohiva.play.silhouette.impl.util._
import com.mohiva.play.silhouette.api.util._
import services._

import models.User

import play.api.Play
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {

  override def configure(): Unit = {
    //    bind[TagDAO].to[TagDAOImpl]
    //    bind[UserDAO].to[UserDAOImpl]
    bind[PostService].to[PostServiceImpl]
    bind[TagService].to[TagServiceImpl]
    bind[UserService].to[UserServiceImpl]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator(32))
    bind[HTTPLayer].toInstance(new PlayHTTPLayer)
    bind[EventBus].toInstance(EventBus())
  }

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
    userService: UserService,
    authenticatorService: AuthenticatorService[BearerTokenAuthenticator],
    eventBus: EventBus): Environment[User, BearerTokenAuthenticator] = {

    Environment[User, BearerTokenAuthenticator](
      userService,
      authenticatorService,
      Seq(),
      eventBus
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
   * Provides the mobile provider.
   *
   * @return The mobile provider.
   */
  @Provides
  def provideMobileProvider(): MobileProvider = {
    new MobileProvider()
  }

}
