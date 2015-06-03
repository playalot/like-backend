package services

import models.{ Mark, Comment, User, Post }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait PostService {

  def countByUserId(userId: Long): Future[Long]

  def searchByTag(page: Int = 0, pageSize: Int = 20, name: String): Future[Seq[(Post, User)]]

  def getPostById(postId: Long): Future[Option[(Post, User)]]

  def getMarksForPost(postId: Long, page: Int = 0, userId: Option[Long] = None): Future[(Seq[(Long, String, Long, User)], Set[Long], Map[Long, Int], Seq[(Comment, User, Option[User])])]

  def deletePostById(postId: Long, userId: Long): Future[Either[Boolean, String]]

  def addMark(postId: Long, authorId: Long, tagName: String, userId: Long): Future[Mark]

}
