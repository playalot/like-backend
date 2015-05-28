package controllers

import javax.inject.Inject

import play.api.i18n.{ Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import services.{ PostService, TagService }
import utils.QiniuUtil

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/28/15
 */
class PostController @Inject() (
    val messagesApi: MessagesApi,
    tagService: TagService,
    postService: PostService) extends BaseController {

  def getPost(id: Long) = UserAwareAction.async { implicit request =>
    postService.getPostById(id).map {
      case Some(postAndUser) => {
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
      }
      case None =>
        Ok(Json.obj(
          "code" -> 4020,
          "field" -> "post_id",
          "message" -> Messages("postNotFound")
        ))
    }
  }

  def getPostMarks(id: Long, page: Int) = UserAwareAction.async { implicit request =>
    postService.getPostById(id).flatMap {
      case Some(postAndUser) =>

        postService.getMarksForPost(id, page, request.userId).map { result =>

          val scores = result._3
          val likes = result._2
          val comments = result._4.groupBy(_._1.markId)

          val marksJson = result._1.map { marks =>
            val totalComments = comments.get(marks._1).getOrElse(Seq()).length
            val commentsJson = comments.get(marks._1).map { list =>
              list.take(3).map { values =>
                Json.obj(
                  "comment_id" -> values._1.id,
                  "content" -> values._1.content,
                  "created" -> values._1.created,
                  "location" -> values._1.location,
                  "user" -> Json.obj(
                    "user_id" -> values._2.id,
                    "nickname" -> values._2.nickname,
                    "avatar" -> QiniuUtil.getAvatar(values._2.avatar, "small"),
                    "likes" -> values._2.likes
                  ),
                  "reply" -> values._3.map(reply => Json.obj(
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

}
