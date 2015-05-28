package models

/**
 * Created by Guan Guan
 * Date: 5/21/15
 */
case class Comment(
    id: Option[Long],
    markId: Long,
    userId: Long,
    replyId: Option[Long],
    content: String,
    created: Long,
    location: Option[String]) {

  require(content.length > 0)
}
