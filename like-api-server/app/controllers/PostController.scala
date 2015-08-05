package controllers

import javax.inject.{ Named, Inject }

import akka.actor.ActorRef
import com.likeorz.common.ClassifyPost
import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import com.likeorz.event.LikeEvent
import com.likeorz.models.{ Notification, Post, Report }
import com.likeorz.services._
import services.PushService
import utils.QiniuUtil

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/28/15
 */
class PostController @Inject() (
    @Named("event-producer-actor") eventProducerActor: ActorRef,
    @Named("classification-actor") classificationActor: ActorRef,
    val messagesApi: MessagesApi,
    tagService: TagService,
    markService: MarkService,
    userService: UserService,
    postService: PostService,
    notificationService: NotificationService,
    pushService: PushService) extends BaseController {

  case class PostCommand(content: String, `type`: Option[String], tags: Seq[String], place: Option[String], location: Option[Seq[Double]])

  implicit val postCommandReads = (
    (__ \ 'content).read[String] and
    (__ \ 'type).readNullable[String] and
    (__ \ 'tags).read[Seq[String]] and
    (__ \ 'place).readNullable[String] and
    (__ \ 'location).readNullable[Seq[Double]]
  )(PostCommand.apply _)

  /**
   * Get Qiniu upload token
   */
  def getQiniuUploadToken = SecuredAction {
    success(Messages("success.uploadToken"),
      Json.obj("upload_token" -> QiniuUtil.getUploadToken()))
  }

  /**
   * Publish a post
   */
  def publishPost = SecuredAction.async(parse.json) { implicit request =>
    val screenWidth = request.headers.get("LIKE_SCREEN_WIDTH").getOrElse("1280").toInt
    request.body.validate[PostCommand].fold(
      errors => {
        Future.successful(error(4012, Messages("invalid.postJson")))
      },
      postCommand => {
        postService.insert(Post(None, postCommand.content, postCommand.`type`.getOrElse("PHOTO"),
          request.userId, System.currentTimeMillis / 1000, System.currentTimeMillis / 1000,
          postCommand.place, postCommand.location.map(_.take(2).mkString(" ")))).flatMap { post =>

          // log event
          eventProducerActor ! LikeEvent(None, "publish", "user", request.userId.toString, Some("post"), Some(post.identify),
            properties = Json.obj("tags" -> Json.toJson(postCommand.tags), "img" -> postCommand.content))

          val futures = postCommand.tags
            .filter(t => t.length <= 13 && t.length >= 1)
            .map(tag => postService.addMark(post.id.get, request.userId, tag, request.userId).map(mark => (tag, mark)))

          for {
            author <- userService.getUserInfo(request.userId)
            results <- Future.sequence(futures)
          } yield {
            if (author.likes.toLong > 200) {
              // classify post
              classificationActor ! ClassifyPost(post.id.get, postCommand.tags, post.created)
            }
            val marksJson = results.map { tagAndMark =>
              Json.obj(
                "mark_id" -> tagAndMark._2.id.get,
                "tag" -> tagAndMark._1,
                "likes" -> 1,
                "is_liked" -> 1
              )
            }
            success(Messages("success.publish"), Json.obj(
              "post_id" -> post.id.get,
              "content" -> QiniuUtil.getSizedImage(post.content, screenWidth),
              "type" -> post.`type`,
              "description" -> post.description,
              "created" -> post.created,
              "place" -> post.place,
              "location" -> Json.toJson(postCommand.location),
              "user" -> Json.obj(
                "user_id" -> request.userId,
                "nickname" -> author.nickname,
                "avatar" -> QiniuUtil.getAvatar(author.avatar, "small"),
                "likes" -> author.likes
              ),
              "marks" -> Json.toJson(marksJson)
            ))
          }
        }
      }
    )
  }

  /** Get Post summary and author info */
  def getPost(id: Long) = UserAwareAction.async { implicit request =>
    postService.getPostById(id).flatMap {
      case Some(post) =>
        userService.getUserInfo(post.userId).map { user =>
          val location = try {
            post.location.map(_.split(" ").map(_.toDouble))
          } catch {
            case _: Throwable => None
          }
          success(Messages("success.found"), Json.obj(
            "post_id" -> id,
            "type" -> post.`type`,
            "content" -> QiniuUtil.getRaw(post.content),
            "description" -> post.description,
            "created" -> post.created,
            "place" -> post.place,
            "location" -> location,
            "user" -> Json.obj(
              "user_id" -> post.userId,
              "nickname" -> user.nickname,
              "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
              "likes" -> user.likes
            )
          ))
        }
      case None => Future.successful(error(4020, Messages("invalid.postId")))
    }
  }

  /** Delete Post */
  def deletePost(id: Long) = SecuredAction.async { implicit request =>
    postService.getPostById(id).flatMap {
      case Some(post) =>
        if (post.userId != request.userId) {
          Future.successful(error(4023, Messages("no.permission")))
        } else {
          for {
            p <- postService.deletePostById(id, request.userId)
            n <- notificationService.deleteAllNotificationForPost(id)
            r <- postService.recordDelete(post.content)
          } yield {
            success(Messages("success.deletePost"))
          }
        }
      case None => Future.successful(error(4020, Messages("invalid.postId")))
    }
  }

  /** Get Post marks and comments */
  def getPostMarks(id: Long, page: Int) = UserAwareAction.async { implicit request =>
    postService.getPostById(id).flatMap {
      case Some(post) =>
        postService.getMarksForPost(id, page, request.userId).map { result =>

          // result : (markList, likeList, cachedMarks, commentsOnMarks)
          val scores = result._3
          val likes = result._2
          val comments = result._4.groupBy(_._1.markId)

          // log event
          eventProducerActor ! LikeEvent(None, "view", "user", request.userId.toString, Some("post"), Some(id.toString),
            properties = Json.obj("tags" -> Json.toJson(result._1.map(_._2))))

          // Generate json for each mark
          val marksJson = result._1.sortBy(m => -scores(m._1)).map { mark =>
            val totalComments = comments.getOrElse(mark._1, Seq()).length
            // Comments json
            val commentsJson = comments.get(mark._1).map { list =>
              // Take latest 3 comments
              list.sortBy(_._1.created).takeRight(3).map { row =>
                Json.obj(
                  "comment_id" -> row._1.id,
                  "content" -> row._1.content,
                  "created" -> row._1.created,
                  "place" -> row._1.place,
                  "user" -> Json.obj(
                    "user_id" -> row._2.id,
                    "nickname" -> row._2.nickname,
                    "avatar" -> QiniuUtil.getAvatar(row._2.avatar, "small"),
                    "likes" -> row._2.likes
                  ),
                  "reply" -> row._3.map(reply => Json.obj(
                    "user_id" -> reply.id,
                    "nickname" -> reply.nickname,
                    "avatar" -> QiniuUtil.getAvatar(reply.avatar, "small"),
                    "likes" -> reply.likes
                  ))
                )
              }
            }

            Json.obj(
              "mark_id" -> mark._1,
              "tag" -> mark._2,
              "likes" -> scores(mark._1),
              "is_liked" -> likes.contains(mark._1),
              "created" -> mark._3,
              "user" -> Json.obj(
                "user_id" -> mark._4,
                "nickname" -> mark._5,
                "avatar" -> QiniuUtil.getAvatar(mark._6, "small"),
                "likes" -> mark._7
              ),
              "total_comments" -> totalComments,
              "comments" -> Json.toJson(commentsJson.getOrElse(Seq()))
            )
          }
          success(Messages("success.found"), Json.obj("marks" -> Json.toJson(marksJson)))
        }
      case None => Future.successful(error(4020, Messages("invalid.postId")))
    }
  }

  /** Add a mark to the post */
  def addMark(postId: Long) = SecuredAction.async(parse.json) { implicit request =>
    (request.body \ "tag").asOpt[String] match {
      case Some(tag) =>
        if (tag.length > 13)
          Future.successful(error(4025, Messages("invalid.tagMaxLength")))
        else if (tag.length < 1)
          Future.successful(error(4025, Messages("invalid.tagMinLength")))
        else
          postService.getPostById(postId).flatMap {
            case Some(post) =>
              // log event
              eventProducerActor ! LikeEvent(None, "mark", "user", request.userId.toString, Some("post"), Some(postId.toString), properties = Json.obj("tag" -> tag))
              for {
                nickname <- userService.getNickname(request.userId)
                mark <- postService.addMark(postId, post.userId, tag, request.userId)
              } yield {
                if (request.userId != post.userId) {
                  val notifyMarkUser = Notification(None, "MARK", post.userId, request.userId, System.currentTimeMillis / 1000, Some(tag), Some(postId))
                  for {
                    notify <- notificationService.insert(notifyMarkUser)
                    count <- notificationService.countForUser(post.userId)
                  } yield {
                    pushService.sendPushNotificationToUser(post.userId, Messages("notification.mark", nickname, tag), count)
                  }
                }
                success(Messages("success.mark"), Json.obj(
                  "mark_id" -> mark.id.get,
                  "tag" -> tag,
                  "likes" -> 1,
                  "is_liked" -> 1
                ))
              }
            case None => Future.successful(error(4024, Messages("invalid.postId")))
          }
      case None => Future.successful(error(4025, Messages("invalid.tagField")))
    }
  }

  /** Report abuse of a post */
  def report(postId: Long) = SecuredAction.async { implicit request =>
    postService.report(Report(None, request.userId, postId)).map { _ =>
      success("success.report")
    }
  }

}
