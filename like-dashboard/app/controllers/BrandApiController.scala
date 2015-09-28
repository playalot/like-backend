package controllers

import javax.inject.Inject

import com.likeorz.models.Entity
import com.likeorz.services._
import com.mohiva.play.silhouette.api.{ Silhouette, Environment }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.qiniu.storage.UploadManager
import com.qiniu.util.Auth
import models.Admin
import play.api.{ Logger, Play }
import play.api.i18n.MessagesApi
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits._
import services.DashboardService
import utils.QiniuUtil

import scala.concurrent.Future

class BrandApiController @Inject() (
    val messagesApi: MessagesApi,
    val env: Environment[Admin, CookieAuthenticator],
    dashboardService: DashboardService,
    userService: UserService,
    postService: PostService,
    markService: MarkService,
    promoteService: PromoteService) extends Silhouette[Admin, CookieAuthenticator] {

  implicit val brandFormats = Json.format[Entity]

  def fetchBrandList(page: Int, pageSize: Int, filter: String) = SecuredAction.async {
    for {
      entities <- promoteService.getEntities(pageSize, page, filter)
    } yield {
      val jsonList = Json.toJson(entities.map { row =>
        val (entity, promoteOpt) = row
        Json.obj(
          "id" -> entity.id,
          "name" -> entity.name,
          "avatar" -> QiniuUtil.resizeImage(entity.avatar, 100),
          "description" -> entity.description,
          "isPromoted" -> promoteOpt.isDefined
        )
      })
      Ok(Json.obj(
        "brands" -> jsonList
      ))
    }
  }

  def deletePromoteBrand(id: Long) = SecuredAction.async {
    promoteService.unPromoteEntity(id).map(_ => Ok)
  }

  def promoteBrand(id: Long) = SecuredAction.async {
    promoteService.promoteEntity(id).map(_ => Ok)
  }

  def insertBrand() = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Entity].fold(
      errors => {
        Future.successful(BadRequest)
      },
      entity => {
        promoteService.insertEntity(entity).map(e => Ok(Json.toJson(e)))
      })
  }

  val QiniuAccessKey = Play.current.configuration.getString("qiniu.accesskey").get
  val QiniuSecretKey = Play.current.configuration.getString("qiniu.secretkey").get

  val DefaultBucket = Play.current.configuration.getString("qiniu.bucket").get

  val QiniuUploadManager = new UploadManager()
  val QiniuAuth = Auth.create(QiniuAccessKey, QiniuSecretKey)

  def getUploadToken(bucket: String = DefaultBucket): String = {
    QiniuAuth.uploadToken(bucket)
  }

  def uploadBrandImageToQiniu(id: Long) = SecuredAction.async(parse.multipartFormData) { implicit request =>
    val file = request.body.files.head.ref
    promoteService.getEntity(id).map {
      case Some(entity) =>
        val key = if (entity.avatar == "") {
          val key = "entity_" + id + "_" + (System.currentTimeMillis() / 1000) + ".jpg"
          val data = scala.io.Source.fromFile(file.file).map(_.toByte).toArray
          val res = QiniuUploadManager.put(data, key, QiniuAuth.uploadToken(DefaultBucket))
          promoteService.updateEntity(entity.copy(avatar = key))
          key
        } else {
          val key = entity.avatar
          val data = scala.io.Source.fromFile(file.file).map(_.toByte).toArray
          val res = QiniuUploadManager.put(data, key, QiniuAuth.uploadToken(DefaultBucket, entity.avatar))
          Logger.info(res.bodyString())
          key
        }
        Ok(key)
      case None => BadRequest
    }
  }

  def getBrand(id: Long) = SecuredAction.async {
    promoteService.getEntity(id).map {
      case Some(entity) => Ok(Json.toJson(entity).as[JsObject] ++ Json.obj("image" -> QiniuUtil.resizeImage(entity.avatar, 200)))
      case None         => BadRequest
    }
  }

  def deleteBrand(id: Long) = SecuredAction.async {
    promoteService.deleteEntity(id).map(_ => Ok)
  }

  def updateBrand(id: Long) = SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[Entity].fold(
      errors => {
        println(errors)
        Future.successful(BadRequest)
      },
      entity => {
        promoteService.updateEntity(entity.copy(id = Some(id))).map(e => Ok(Json.toJson(e)))
      })
  }

}
