package services

import models._

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait PostService {

  def insert(post: Post): Future[Post]

  def countByUserId(userId: Long): Future[Long]

  def getPostsByUserId(userId: Long, page: Int): Future[Seq[(Post, Seq[(Long, String, Int)])]]

  def searchByTag(page: Int = 0, pageSize: Int = 20, name: String): Future[Seq[(Post, User)]]

  def getPostById(postId: Long): Future[Option[(Post, User)]]

  def getMarksForPost(postId: Long, page: Int = 0, userId: Option[Long] = None): Future[(Seq[(Long, String, Long, User)], Set[Long], Map[Long, Int], Seq[(Comment, User, Option[User])])]

  def deletePostById(postId: Long, userId: Long): Future[Unit]

  def addMark(postId: Long, authorId: Long, tagName: String, userId: Long): Future[Mark]

  def report(report: Report): Future[Report]
}
