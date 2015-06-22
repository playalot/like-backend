package services

import com.likeorz.models.Tag

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait TagService {

  def suggestTagsForUser(userId: Long): Future[Seq[Tag]]

  def autoComplete(name: String): Future[Seq[Tag]]

  def hotTags: Future[Seq[Tag]]

}
