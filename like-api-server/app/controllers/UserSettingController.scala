package controllers

import javax.inject.Inject

import com.likeorz.models.UserTag
import com.likeorz.services._
import com.likeorz.utils.GlobalConstants.{ DefaultAvatar, DefaultCover }
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class UserSettingController @Inject() (
    val messagesApi: MessagesApi,
    userSettingService: UserSettingService,
    userService: UserService,
    postService: PostService,
    tagService: TagService,
    notificationService: NotificationService,
    pushService: PushService) extends BaseController {

  def updateNickname() = (SecuredAction andThen BannedUserCheckAction).async(parse.json) { implicit request =>
    (request.body \ "nickname").asOpt[String] match {
      case Some(nickname) =>
        userService.nicknameExists(nickname.trim).flatMap {
          case true => Future.successful(error(4040, Messages("invalid.nicknameExist")))
          case false =>
            if (nickname.trim.length > 20) {
              Future.successful(error(4040, Messages("invalid.nicknameLong")))
            } else if (nickname.trim.length < 2) {
              Future.successful(error(4040, Messages("invalid.nicknameShort")))
            } else {
              userSettingService.updateNickname(request.userId, nickname.trim).map(_ => success(Messages("success.nickname")))
            }
        }
      case None => Future.successful(error(4040, Messages("invalid.nickname")))
    }
  }

  def updateAvatar() = (SecuredAction andThen BannedUserCheckAction).async(parse.json) { implicit request =>
    (request.body \ "avatar").asOpt[String] match {
      case Some(avatar) =>
        for {
          user <- userService.findById(request.userId)
          result <- userSettingService.updateAvatar(request.userId, avatar)
        } yield {
          if (user.get.avatar != DefaultAvatar) {
            postService.recordDelete(user.get.avatar)
          }
          success(Messages("success.avatar"))
        }
      case None =>
        Future.successful(error(4041, Messages("invalid.avatar")))
    }
  }

  def updateCover() = (SecuredAction andThen BannedUserCheckAction).async(parse.json) { implicit request =>
    (request.body \ "cover").asOpt[String] match {
      case Some(avatar) =>
        for {
          user <- userService.findById(request.userId)
          result <- userSettingService.updateCover(request.userId, avatar)
        } yield {
          if (user.get.cover != DefaultCover) {
            postService.recordDelete(user.get.cover)
          }
          success(Messages("success.cover"))
        }
      case None =>
        Future.successful(error(4041, Messages("invalid.cover")))
    }
  }

  def choosePreferTags = SecuredAction.async(parse.json) { implicit request =>
    val groupIds = (request.body \ "groups").as[Seq[Long]]
    val tagIds = (request.body \ "tags").as[Seq[Long]]

    tagService.getGroupedTags.flatMap { results =>
      val tags = results.filter(g => groupIds.contains(g._1.id.get))
        .flatMap { pair =>
          val topN = pair._2.length / 3
          pair._2.sortBy(_.usage).reverse.take(topN)
        }
      val userTags = tags.map(tag => UserTag(request.userId, tag.id.get)).toSeq

      for {
        subscribedIds <- tagService.getUserHistoryTagIds(request.userId)
        result <- tagService.subscribeTags(userTags.filterNot(t => subscribedIds.contains(t.tagId)))
      } yield {
        success(Messages("success.found"))
      }
    }
  }

}
