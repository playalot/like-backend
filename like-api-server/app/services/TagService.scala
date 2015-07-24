package services

import com.likeorz.models.{ User, Tag }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait TagService {

  def suggestTagsForUser(userId: Long): Future[Seq[String]]

  def autoComplete(name: String): Future[Seq[Tag]]

  def hotTags(num: Int): Future[Seq[String]]

  def hotUsersForTag(tag: String, num: Int): Future[Seq[User]]

  def validTag(tag: String): Boolean

}
