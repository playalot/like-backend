package com.likeorz.services

import com.likeorz.models._

import scala.concurrent.Future

trait PostService {

  def insert(post: Post): Future[Post]

  def countPostsForUser(userId: Long): Future[Long]

  def countFavoriteForUser(userId: Long): Future[Long]

  def getPostById(postId: Long): Future[Option[Post]]

  def getPostsByUserId(userId: Long, page: Int, pageSize: Int): Future[Seq[(Post, Seq[(Long, String, Int)])]]

  def getPostsByIds(ids: Seq[Long]): Future[Seq[(Post, Seq[(Long, String, Int)])]]

  def getPostsByIdsSimple(ids: Seq[Long]): Future[Seq[Post]]

  def searchByTag(page: Int = 0, pageSize: Int = 20, name: String): Future[Seq[Post]]

  def searchByTagAndTimestamp(name: String = "%", pageSize: Int = 18, timestamp: Option[Long]): Future[Seq[Post]]

  def findHotPostForTag(name: String, page: Int = 0, pageSize: Int = 20): Future[Seq[(Post, User)]]

  def getTagPostImage(name: String): Future[Option[String]]

  def getMarksForPost(postId: Long, page: Int = 0, userId: Option[Long] = None): Future[(Seq[(Long, String, Long, Long, String, String, Long)], Set[Long], Map[Long, Int], Seq[(Comment, UserInfo, Option[UserInfo])])]

  def deletePostById(postId: Long, userId: Long): Future[Unit]

  def addMark(postId: Long, authorId: Long, tagName: String, userId: Long): Future[Mark]

  def updatePostTimestamp(postId: Long): Future[Unit]

  def favorite(postId: Long, userId: Long): Future[Favorite]

  def unFavorite(postId: Long, userId: Long): Future[Unit]

  def isFavorited(postId: Long, userId: Long): Future[Boolean]

  def report(report: Report): Future[Report]

  def getRecommendedPosts(pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]]

  def getFollowingPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]]

  def getPostIdsForUser(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]]

  def getPostsForUser(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Post]]

  def getPersonalizedPostsForUser(userId: Long, ratio: Double, pageSize: Int, timestamp: Option[Long]): Seq[Long]

  def getTaggedPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]]

  def getMyPostTimelineFeeds(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[TimelineFeed]]

  def getEditorPickTimelineFeeds(pageSize: Int, timestamp: Option[Long]): Future[Seq[TimelineFeed]]

  def getBasedOnTagTimelineFeeds(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[TimelineFeed]]

  def getUserTags(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Set[String]]

  def getRecentPosts(pageSize: Int, timestamp: Option[Long], filter: Option[String]): Future[Seq[Long]]

  def getRecentPostsForUser(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]]

  def getFavoritePostsForUser(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Map[Long, Long]]

  def recordDelete(photo: String): Future[Unit]

  def get30DayHotUsers(num: Int): Future[Seq[User]]

  def get7DayHotUsers(num: Int): Future[Seq[User]]
}
