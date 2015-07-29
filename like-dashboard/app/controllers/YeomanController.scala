package controllers

import javax.inject.Inject

import play.api._
import play.api.mvc._
import play.api.Play.current
import java.io.File
import services.AdminService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.Admin
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.{ AnyContent, Action }

import scala.concurrent.Future

class YeomanController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[Admin, CookieAuthenticator],
    adminService: AdminService,
    authInfoRepository: AuthInfoRepository,
    credentialsProvider: CredentialsProvider,
    configuration: Configuration,
    clock: Clock) extends Silhouette[Admin, CookieAuthenticator] {

  def index = SecuredAction.async { implicit request =>
    at("index.html").apply(request)
  }

  def redirectRoot(base: String = "/ui/") = SecuredAction {
    request =>
      if (base.endsWith("/")) {
        Redirect(base)
      } else {
        Redirect(base + "/")
      }
  }

  def assetHandler(file: String): Action[AnyContent] = {
    Assets.at("/public", file)
  }

  lazy val atHandler: String => Action[AnyContent] = if (play.Play.isProd) assetHandler(_: String) else DevAssets.assetHandler(_: String)

  def at(file: String): Action[AnyContent] = atHandler(file)

}

object DevAssets extends Controller {
  // paths to the grunt compile directory or else the application directory, in order of importance
  val runtimeDirs = Play.configuration.getStringList("yeoman.devDirs")
  val basePaths: List[java.io.File] = runtimeDirs match {
    case Some(dirs) => dirs.asScala.map(Play.application.getFile _).toList
    case None => List(Play.application.getFile("ui/.tmp"), Play.application.getFile("ui/app"),
      //added ui to defaults since the newer projects have bower_components in ui directory instead of ui/app/components
      Play.application.getFile("ui"))
  }

  /**
   * Construct the temporary and real path under the application.
   *
   * The play application path is prepended to the full path, to make sure the
   * absolute path is in the correct SBT sub-project.
   */
  def assetHandler(fileName: String): Action[AnyContent] = Action {
    val targetPaths = basePaths.view map {
      new File(_, fileName)
    } // generate a non-strict (lazy) list of the full paths

    // take the files that exist and generate the response that they would return
    val responses = targetPaths filter {
      file =>
        file.exists()
    } map {
      file =>
        Ok.sendFile(file, inline = true).withHeaders(CACHE_CONTROL -> "no-store")
    }

    // return the first valid path, return NotFound if no valid path exists
    responses.headOption getOrElse NotFound
  }
}