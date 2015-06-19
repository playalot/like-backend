package services

import models.Feedback

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/19/15
 */
trait InfoService {

  def addFeedback(fb: Feedback): Future[Feedback]

}
