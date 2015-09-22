package services

import com.likeorz.models.Post
import models.Page

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 7/7/15
 */
trait DashboardService {

  def list(page: Int = 0, pageSize: Int = 10): Future[Page[(Post, Seq[(Long, String, Int)])]]

  def blockPost(postId: Long, status: Boolean): Future[Boolean]

  def recommendPost(postId: Long, status: Boolean): Future[Int]

  def isPostRecommended(postId: Long): Future[Boolean]

  def isPostBlocked(postId: Long): Future[Boolean]

  def countPostTotalLikes(filter: String): Future[Seq[(Long, Int)]]
}
