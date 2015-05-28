package services

import models.{ User, Post }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait PostService {

  def countByUserId(userId: Long): Future[Long]

  def searchByTag(page: Int = 0, pageSize: Int = 20, name: String): Future[Seq[(Post, User)]]

  def getPostById(postId: Long): Future[Post]
}
