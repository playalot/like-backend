package services

import models.Tag

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait TagService {

  def autoComplete(name: String): Future[Seq[Tag]]

  def hotTags: Future[Seq[Tag]]

}
