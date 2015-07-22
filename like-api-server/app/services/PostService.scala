package services

import com.likeorz.models._

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
trait PostService {

  def insert(post: Post): Future[Post]

  def countByUserId(userId: Long): Future[Long]

  def getPostById(postId: Long): Future[Option[(Post, User)]]

  def getPostsByUserId(userId: Long, page: Int, pageSize: Int): Future[Seq[(Post, Seq[(Long, String, Int)])]]

  def getPostsByIds(ids: Seq[Long]): Future[Seq[(Post, User, Seq[(Long, String, Int)])]]

  def searchByTag(page: Int = 0, pageSize: Int = 20, name: String): Future[Seq[(Post, User)]]

  def findHotPostForTag(name: String, page: Int = 0, pageSize: Int = 20): Future[Seq[(Post, User)]]

  def getTagPostImage(name: String): Future[Option[String]]

  def getMarksForPost(postId: Long, page: Int = 0, userId: Option[Long] = None): Future[(Seq[(Long, String, Long, User)], Map[Long, Int], Seq[(Like, User)], Seq[(Comment, User, Option[User])])]

  def deletePostById(postId: Long, userId: Long): Future[Unit]

  def addMark(postId: Long, authorId: Long, tagName: String, userId: Long): Future[Mark]

  def report(report: Report): Future[Report]

  def getRecommendedPosts(pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]]

  def getFollowingPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]]

  def getTaggedPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]]

  def getRecentPosts(pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]]

  def recordDelete(photo: String): Future[Unit]

  def getPersonalizedPostsForUser(userId: Long, ratio: Double): Seq[Long]

  def get30DayHotUsers(num: Int): Future[Seq[User]]

  def get7DayHotUsers(num: Int): Future[Seq[User]]
}
