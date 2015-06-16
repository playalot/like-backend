package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.{ MarkService, PostService, TagService }
import utils.QiniuUtil

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/28/15
 */
class PostController @Inject() (
    val messagesApi: MessagesApi,
    tagService: TagService,
    markService: MarkService,
    postService: PostService) extends BaseController {

  /**
   * Get Post summary and author info
   */
  def getPost(id: Long) = UserAwareAction.async { implicit request =>
    postService.getPostById(id).map {
      case Some(postAndUser) =>
        val (post, user) = postAndUser
        Ok(Json.obj(
          "code" -> 1,
          "message" -> "Record(s) Found",
          "data" -> Json.obj(
            "post_id" -> id,
            "type" -> post.`type`,
            "content" -> QiniuUtil.getPhoto(post.content, "large"),
            "description" -> post.description,
            "created" -> post.created,
            "user" -> Json.obj(
              "user_id" -> user.id.get.toString,
              "nickname" -> user.nickname,
              "avatar" -> QiniuUtil.getAvatar(user.avatar, "small"),
              "likes" -> user.likes
            )
          )
        ))
      case None =>
        Ok(Json.obj(
          "code" -> 4020,
          "field" -> "post_id",
          "message" -> Messages("postNotFound")
        ))
    }
  }

  /**
   * Delete Post by id
   */
  def deletePost(id: Long) = SecuredAction.async { implicit request =>
    postService.deletePostById(id, request.userId).map {
      case Left(x) => Ok(Json.obj(
        "code" -> 1,
        "message" -> "Post Delete Success"
      ))
      case Right(ex) => Ok(Json.obj(
        "code" -> 4023,
        "message" -> Messages(ex)
      ))
    }
  }

  /**
   * Get Post marks and comments
   */
  def getPostMarks(id: Long, page: Int) = UserAwareAction.async { implicit request =>
    postService.getPostById(id).flatMap {
      case Some(postAndUser) =>

        postService.getMarksForPost(id, page, request.userId).map { result =>

          val scores = result._3
          val likes = result._2
          val comments = result._4.groupBy(_._1.markId)

          val marksJson = result._1.map { marks =>
            val totalComments = comments.getOrElse(marks._1, Seq()).length
            val commentsJson = comments.get(marks._1).map { list =>
              list.reverse.take(3).map { row =>
                Json.obj(
                  "comment_id" -> row._1.id,
                  "content" -> row._1.content,
                  "created" -> row._1.created,
                  "location" -> row._1.location,
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
              "mark_id" -> marks._1,
              "tag" -> marks._2,
              "likes" -> scores(marks._1),
              "is_liked" -> likes.contains(marks._1),
              "created" -> marks._3,
              "user" -> Json.obj(
                "user_id" -> marks._4.id,
                "nickname" -> marks._4.nickname,
                "avatar" -> QiniuUtil.getAvatar(marks._4.avatar, "small"),
                "likes" -> marks._4.likes
              ),
              "total_comments" -> totalComments,
              "comments" -> Json.toJson(commentsJson.getOrElse(Seq()))
            )
          }

          Ok(Json.obj(
            "code" -> 1,
            "message" -> "Record(s) Found",
            "data" -> Json.obj(
              "marks" -> Json.toJson(marksJson)
            )
          ))
        }
      case None => Future.successful(Ok(Json.obj(
        "code" -> 4020,
        "field" -> "post_id",
        "message" -> Messages("postNotFound")
      )))
    }
  }

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

  def addMark(postId: Long) = SecuredAction.async(parse.json) { implicit request =>

    (request.body \ "tag").asOpt[String] match {
      case Some(tag) =>
        if (tag.length > 13)
          Future.successful(Ok(Json.obj(
            "code" -> 4022,
            "field" -> "tag",
            "message" -> Messages("tag.maxLength")
          )))
        else if (tag.length < 1)
          Future.successful(Ok(Json.obj(
            "code" -> 4022,
            "field" -> "tag",
            "message" -> Messages("tag.minLength")
          )))
        else
          postService.getPostById(postId).flatMap {
            case Some(post) =>
              postService.addMark(postId, post._1.userId, tag, request.userId).map { mark =>
                Ok(Json.obj(
                  "code" -> 1,
                  "message" -> "Mark Success",
                  "data" -> Json.obj(
                    "mark_id" -> mark.id.get,
                    "tag" -> tag,
                    "likes" -> 1,
                    "is_liked" -> 1
                  )
                ))
              }
            case None =>
              Future.successful(Ok(Json.obj(
                "code" -> 4022,
                "message" -> Messages("post.notFound")
              )))
          }
      case None =>
        Future.successful(Ok(Json.obj(
          "code" -> 4022,
          "field" -> "tag",
          "message" -> "Field Not Found"
        )))
    }

  }

}
