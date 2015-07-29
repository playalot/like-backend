package services

import com.likeorz.models.{ Post, User }
import models.Page

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 7/7/15
 */
trait DashboardService {

  def list(page: Int = 0, pageSize: Int = 10): Future[Page[(Post, Seq[(Long, String, Int)])]]

  def invisiblePost(postId: Long, status: Boolean): Future[Boolean]

  def recommendPost(postId: Long, status: Boolean): Future[Unit]

  def isPostRecommended(postId: Long): Future[Boolean]

  def isPostInvisible(postId: Long): Future[Boolean]
}
