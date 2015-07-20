package services

import com.likeorz.models.{ User, Post }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 7/7/15
 */
trait PostService {

  def getPostById(postId: Long): Future[Option[(Post, User)]]

  def getPostsByUserId(userId: Long, page: Int, pageSize: Int): Future[Seq[(Post, Seq[(Long, String, Int)])]]

  def getPostsByIds(ids: Set[Long]): Future[Seq[(Post, User, Seq[(Long, String, Int)])]]

  def searchByTag(page: Int = 0, pageSize: Int = 20, name: String): Future[Seq[(Post, User)]]

  def getPersonalCategoryPosts(userId: Long): Seq[Long]

  def getRandomUsers(): Future[Seq[User]]
}
