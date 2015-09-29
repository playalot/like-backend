package com.likeorz.services

import javax.inject.Inject

import com.likeorz.models._
import com.likeorz.dao._
import com.likeorz.utils.{ FutureUtils, RedisCacheClient, KeyUtils }
import org.joda.time.DateTime
import org.nlpcn.commons.lang.jianfan.JianFan
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.db.slick.{ HasDatabaseConfigProvider, DatabaseConfigProvider }
import slick.driver.JdbcProfile

import scala.concurrent.Future
import scala.util.Random

class PostServiceImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends PostService
    with PostsComponent with UsersComponent
    with TagsComponent with MarksComponent
    with LikesComponent with CommentsComponent
    with RecommendsComponent with FollowsComponent
    with ReportsComponent with DeletedPhotosComponent
    with FavoritesComponent with UserTagsComponent
    with UserInfoComponent
    with HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  override def insert(post: Post): Future[Post] = {
    db.run(posts returning posts.map(_.id) += post).map { id =>
      RedisCacheClient.hincrBy(KeyUtils.user(post.userId), "posts", 1)
      post.copy(id = Some(id))
    }
  }

  override def countPostsForUser(userId: Long): Future[Long] = {
    RedisCacheClient.hget(KeyUtils.user(userId), "posts") match {
      case Some(number) => Future.successful(number.toLong)
      case None =>
        db.run(posts.filter(_.userId === userId).length.result).map { number =>
          RedisCacheClient.hset(KeyUtils.user(userId), "posts", number.toString)
          number
        }
    }
  }

  override def countFavoriteForUser(userId: Long): Future[Long] = {
    db.run(favorites.filter(_.userId === userId).length.result).map(_.toLong)
  }

  override def getPostsByUserId(userId: Long, page: Int, pageSize: Int): Future[Seq[(Post, Seq[(Long, String, Int)])]] = {
    db.run(posts.filter(_.userId === userId).sortBy(_.created.desc).drop(page * pageSize).take(pageSize).result).flatMap { posts =>
      val futures = posts.map { post =>
        val cachedMarks = RedisCacheClient.zrevrangeByScoreWithScores("post_mark:" + post.id.get).map(v => (v._1.toLong, v._2.toInt)).toMap
        if (cachedMarks.nonEmpty) {
          val markIds = cachedMarks.keySet.mkString(", ")
          val query = sql"""select m.id, t.tag from mark m inner join tag t on m.tag_id = t.id  where m.id in (#$markIds)""".as[(Long, String)]
          db.run(query).map { list =>
            val markList = list.map(row => (row._1, row._2, cachedMarks.getOrElse(row._1, 0)))
            (post, markList)
          }
        } else {
          Future.successful((post, Seq()))
        }
      }
      Future.sequence(futures.toList)
    }
  }

  override def getPostsByIds(ids: Seq[Long]): Future[Seq[(Post, Seq[(Long, String, Int)])]] = {
    if (ids.isEmpty) {
      Future.successful(Seq[(Post, Seq[(Long, String, Int)])]())
    } else {
      db.run(posts.filter(_.id inSet ids).result).flatMap { posts =>
        val futures = posts.map { post =>
          val cachedMarks = RedisCacheClient.zrevrangeByScoreWithScores("post_mark:" + post.id.get).map(v => (v._1.toLong, v._2.toInt)).toMap
          if (cachedMarks.nonEmpty) {
            val markIds = cachedMarks.keySet.mkString(", ")
            val query = sql"""select m.id, t.tag from mark m inner join tag t on m.tag_id = t.id  where m.id in (#$markIds)""".as[(Long, String)]
            db.run(query).map { list =>
              val marklist = list.map(row => (row._1, row._2, cachedMarks.getOrElse(row._1, 0)))
              (post, marklist)
            }
          } else {
            Future.successful((post, Seq()))
          }
        }
        Future.sequence(futures.toList)
      }
    }
  }

  def getPostsByIdsSimple(ids: Seq[Long]): Future[Seq[Post]] = {
    FutureUtils.timedFuture("getPostsByIdsSimple:" + ids.size) {
      if (ids.isEmpty) {
        Future.successful(Seq[Post]())
      } else {
        db.run(posts.filter(_.id inSet ids).result)
      }
    }
  }

  override def searchByTag(page: Int = 0, pageSize: Int = 18, name: String = "%"): Future[Seq[Post]] = {
    val offset = pageSize * page

    val jian = JianFan.f2j(name.trim).toLowerCase
    val fan = JianFan.j2f(name.trim).toLowerCase

    val query = if (jian == fan) {
      sql"""SELECT DISTINCT p.id FROM post p INNER JOIN mark m ON p.id=m.post_id where m.tag_id in (SELECT id FROM tag WHERE tag LIKE '%#${jian}%') order by p.created desc limit $offset,$pageSize""".as[Long]
    } else {
      sql"""SELECT DISTINCT p.id FROM post p INNER JOIN mark m ON p.id=m.post_id where m.tag_id in (SELECT id FROM tag WHERE tag LIKE '%#${jian}%' OR tag like '%#${fan}%') order by p.created desc limit $offset,$pageSize""".as[Long]
    }
    FutureUtils.timedFuture("search tag: " + name) {
      db.run(query).flatMap { postIds =>
        val q = (for {
          post <- posts if post.id inSet postIds
        } yield post).sortBy(_.created.desc)
        db.run(q.result)
      }
    }
  }

  override def searchByTagAndTimestamp(name: String = "%", pageSize: Int, timestamp: Option[Long]): Future[Seq[Post]] = {

    val jian = JianFan.f2j(name).toLowerCase
    val fan = JianFan.j2f(name).toLowerCase

    val query = if (timestamp.isDefined) {
      sql"""SELECT DISTINCT m.post_id FROM mark m INNER JOIN tag t ON m.tag_id=t.id WHERE (t.tag like '%#${jian}%' OR t.tag like '%#${fan}%') AND m.created<${timestamp.get} order by m.created desc limit $pageSize""".as[Long]
    } else {
      sql"""SELECT DISTINCT m.post_id FROM mark m INNER JOIN tag t ON m.tag_id=t.id WHERE t.tag like '%#${jian}%' OR t.tag like '%#${fan}%' order by m.created desc limit $pageSize""".as[Long]
    }

    db.run(query).flatMap { postIds =>
      val q = (for {
        post <- posts if post.id inSet postIds
      } yield post).sortBy(_.created.desc)
      db.run(q.result)
    }
  }

  @deprecated("legacy", "1.1.0")
  override def findHotPostForTag(name: String, page: Int = 0, pageSize: Int = 20): Future[Seq[(Post, User)]] = {
    val offset = pageSize * page
    val ts = DateTime.now().minusDays(30).getMillis / 1000
    val query = (for {
      ((post, mark), tag) <- posts join marks on (_.id === _.postId) join tags on (_._2.tagId === _.id)
      if tag.name.toLowerCase === name.toLowerCase && post.created > ts
    } yield post.id)
      .sortBy(_.desc)
      .drop(offset)
      .take(100)
    db.run(query.result).flatMap { pool =>
      val ids = Random.shuffle(pool).take(pageSize)
      val q = for {
        (post, user) <- posts join users on (_.userId === _.id) if post.id inSet ids
      } yield (post, user)
      db.run(q.result)
    }
  }

  @deprecated("legacy", "1.1.0") @deprecated("legacy", "1.1.0")
  override def getTagPostImage(name: String): Future[Option[String]] = {
    val query = for {
      (post, mark) <- posts join marks on (_.id === _.postId)
      if mark.tagName.toLowerCase === name
    } yield post.content
    db.run(query.result.headOption)
  }

  override def getPostById(postId: Long): Future[Option[Post]] = {
    db.run(posts.filter(_.id === postId).result.headOption)
  }

  override def getMarksForPost(postId: Long, page: Int = 0, userId: Option[Long] = None): Future[(Seq[(Long, String, Long, Long, String, String, Long)], Set[Long], Map[Long, Int], Seq[(Comment, UserInfo, Option[UserInfo])])] = {
    // Get top 10 marks for the post from cache
    FutureUtils.timedFuture("get marks for post: " + postId) {
      val cachedMarks = RedisCacheClient.zrevrangeByScoreWithScores("post_mark:" + postId).map(v => (v._1.toLong, v._2.toInt)).toMap
      Logger.debug("Cached marks: " + cachedMarks)

      // Query marks with tagname and user order by created desc
      // TODO: user from cache
      val markIds = cachedMarks.keySet.mkString(", ")
      val marksQuery = sql"""SELECT m.id, t.tag, m.created, m.user_id, u.nickname, u.avatar, u.likes FROM mark m INNER JOIN tag t ON m.tag_id=t.id INNER JOIN user u ON m.user_id=u.id WHERE m.id in (#$markIds)""".as[(Long, String, Long, Long, String, String, Long)]

      //    val mQuery = (for {
      //      ((mark, tag), user) <- marks join tags on (_.tagId === _.id) join users on (_._1.userId === _.id)
      //      if mark.id inSet cachedMarks.keySet
      //    } yield (mark.id, tag.tagName, mark.created, user))

      //    val likesQuery = for {
      //      like <- likes.filter(x => x.userId === userId.getOrElse(0L) && (x.markId inSet cachedMarks.keySet))
      //    } yield like.markId

      // Query all likes on the given set of marks
      val likesQuery = for {
        like <- likes.filter(x => x.userId === userId.getOrElse(0L) && (x.markId inSet cachedMarks.keySet))
      } yield like.markId

      for {
        markList <- if (cachedMarks.keySet.nonEmpty) db.run(marksQuery) else Future.successful(Seq())
        likeList <- db.run(likesQuery.result)
        commentList <- db.run(comments.filter(_.postId === postId).sortBy(_.created.desc).result)
        uids <- Future.successful {
          commentList.map(_.userId) ++ commentList.flatMap(_.replyId)
        }
        users <- db.run(userinfo.filter(_.id inSet (uids)).result)
      } yield {
        val userInfoMap = users.map(u => (u.id -> u)).toMap
        val commentsOnMarks = commentList.map { c =>
          (c, userInfoMap(c.userId), c.replyId.map(userInfoMap(_)))
        }
        (markList, likeList.toSet, cachedMarks, commentsOnMarks)
      }
    }
  }

  override def deletePostById(postId: Long, authorId: Long): Future[Unit] = {
    db.run(marks.filter(_.postId === postId).result).flatMap { markResults =>
      var total: Double = 0
      markResults.foreach { mark =>
        val likeNum = RedisCacheClient.zscore("post_mark:" + postId, mark.identify).getOrElse(0.0)
        RedisCacheClient.hincrBy(KeyUtils.user(mark.userId), "likes", -likeNum.toLong)
        total += likeNum
      }
      RedisCacheClient.del("post_mark:" + postId)
      RedisCacheClient.hincrBy(KeyUtils.user(authorId), "posts", -1)

      val markIds = markResults.map(_.id.getOrElse(-1L))
      for {
        deleteFavorite <- db.run(favorites.filter(_.postId === postId).delete)
        deleteLikes <- db.run(likes.filter(_.markId inSet markIds).delete)
        deleteComments <- db.run(comments.filter(_.postId === postId).delete)
        deleteMarks <- db.run(marks.filter(_.postId === postId).delete)
        deletePost <- db.run(posts.filter(_.id === postId).delete)
      } yield {}
    }
  }

  override def addMark(postId: Long, authorId: Long, tagName: String, userId: Long): Future[Mark] = {
    db.run(tags.filter(_.name === tagName).take(1).result.headOption).flatMap {
      case Some(tag) => Future.successful(tag)
      case None      => db.run(tags returning tags.map(_.id) += Tag(None, tagName)).map(id => Tag(Some(id), tagName))
    }.flatMap { tag =>
      val tagId = tag.id.get
      db.run(marks.filter(m => m.postId === postId && m.tagId === tagId).result.headOption).flatMap {
        case Some(mark) =>
          // If other people create an existed mark, it equals user like the mark
          // !Note this is actually not executed since front-end disallow create same mark
          if (mark.userId != userId) {
            db.run(likes.filter(l => l.markId === mark.id.get && l.userId === userId).result.headOption).flatMap {
              case Some(like) => Future.successful(())
              case None       => db.run(likes += Like(mark.id.get, userId)).map(_ => ())
            } map { _ =>
              // Increase post author likes count
              RedisCacheClient.hincrBy(KeyUtils.user(authorId), "likes", 1)
              // Increase mark author likes count
              if (mark.userId != authorId) RedisCacheClient.hincrBy(KeyUtils.user(mark.userId), "likes", 1)
              // Increase post mark likes count
              RedisCacheClient.zincrby("post_mark:" + postId, 1, mark.identify)
              mark
            }
          } else {
            Future.successful(mark)
          }
        case None =>
          val newMark = Mark(None, postId, tagId, Some(tagName), userId)
          val userTag = UserTag(userId, tagId)
          for {
            // Add a new mark
            mark <- db.run(marks returning marks.map(_.id) += newMark).map(id => newMark.copy(id = Some(id)))
            // Also likes count + 1
            l <- db.run(likes += Like(mark.id.get, userId))
            // If the tag is important, let user subscribe this tag automatically
            ut <- if (tag.group > 0) {
              db.run(userTags.filter(t => t.userId === userId && t.tagId === tagId).result.headOption).flatMap {
                case Some(userTag) => Future.successful(())
                case None          => db.run(userTags += userTag).map(_ => ())
              }
            } else Future.successful(())
          } yield {
            // Increase tag usage count
            RedisCacheClient.zincrby(KeyUtils.tagUsage, 1, mark.tagId.toString)
            // Increase post author likes count
            RedisCacheClient.hincrBy(KeyUtils.user(authorId), "likes", 1)
            // Increase mark author likes count
            if (userId != authorId) RedisCacheClient.hincrBy(KeyUtils.user(userId), "likes", 1)
            // Increase post mark likes count
            RedisCacheClient.zincrby("post_mark:" + postId, 1, mark.identify)
            mark
          }
      }
    }
  }

  override def updatePostTimestamp(postId: Long): Future[Unit] = {
    db.run(posts.filter(_.id === postId).map(_.updated).update(System.currentTimeMillis() / 1000)).map(_ => ())
  }

  override def report(report: Report): Future[Report] = {
    db.run(reports returning reports.map(_.id) += report).map(id => report.copy(id = Some(id)))
  }

  override def favorite(postId: Long, userId: Long): Future[Favorite] = {
    val favorite = Favorite(userId, postId)
    db.run(favorites += favorite).map { _ =>
      RedisCacheClient.hincrBy(KeyUtils.user(userId), "favorites", 1)
      favorite
    }
  }

  override def unFavorite(postId: Long, userId: Long): Future[Unit] = {
    db.run(favorites.filter(fav => fav.userId === userId && fav.postId === postId).delete).map(_ => RedisCacheClient.hincrBy(KeyUtils.user(userId), "favorites", -1))
  }

  override def isFavorited(postId: Long, userId: Long): Future[Boolean] = {
    db.run(favorites.filter(fav => fav.userId === userId && fav.postId === postId).take(1).result.headOption).map(x => x.isDefined)
  }

  override def getEditorPickPostIds(pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    if (timestamp.isDefined) {
      val query = sql"""SELECT r.post_id FROM recommend r INNER JOIN post p ON r.post_id=p.id WHERE p.created < ${timestamp.get} ORDER BY p.created DESC LIMIT $pageSize""".as[Long]
      db.run(query)
    } else {
      val query = sql"""SELECT r.post_id FROM recommend r INNER JOIN post p ON r.post_id=p.id ORDER BY p.created DESC LIMIT $pageSize""".as[Long]
      db.run(query)
    }
  }

  override def getFollowingPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    db.run(follows.filter(_.fromId === userId).map(_.toId).result).flatMap { userIds =>
      if (timestamp.isDefined) {
        db.run(posts.filter(p => p.userId.inSet(userIds.+:(userId)) && p.created < timestamp.get).sortBy(_.created.desc).map(_.id).take(pageSize).result)
      } else {
        db.run(posts.filter(_.userId.inSet(userIds.+:(userId))).sortBy(_.created.desc).map(_.id).take(pageSize).result)
      }
    }
  }

  override def getPostIdsForUser(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    if (timestamp.isDefined) {
      db.run(posts.filter(p => p.userId === userId && p.created < timestamp.get).sortBy(_.created.desc).map(_.id).take(pageSize).result)
    } else {
      db.run(posts.filter(_.userId === userId).sortBy(_.created.desc).map(_.id).take(pageSize).result)
    }
  }

  override def getPostsForUser(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Post]] = {
    if (timestamp.isDefined) {
      db.run(posts.filter(p => p.userId === userId && p.created < timestamp.get).sortBy(_.created.desc).take(pageSize).result)
    } else {
      db.run(posts.filter(_.userId === userId).sortBy(_.created.desc).take(pageSize).result)
    }
  }

  override def getTaggedPosts(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    FutureUtils.timedFuture("getTaggedPosts") {
      db.run(userTags.filter(t => t.userId === userId && t.subscribe === true).map(_.tagId).result).flatMap { tagIds =>
        if (tagIds.nonEmpty) {
          val tIds = Random.shuffle(tagIds).take(500).mkString(", ")
          val query = if (timestamp.isDefined) {
            val ts = timestamp.get
            sql"""SELECT DISTINCT p.id, p.user_id FROM post p INNER JOIN mark m ON p.id=m.post_id WHERE p.created < $ts AND m.tag_id IN (#${tIds}) order by p.created desc limit ${pageSize * 2}""".as[(Long, Long)]
          } else {
            sql"""SELECT DISTINCT p.id, p.user_id FROM post p INNER JOIN mark m ON p.id=m.post_id WHERE m.tag_id IN (#${tIds}) order by p.created desc limit ${pageSize * 2}""".as[(Long, Long)]
          }
          db.run(query).map { results =>
            results.groupBy(_._2).map(_._2.head._1).toSeq.sortBy(pid => pid).reverse.take(pageSize)
          }
        } else {
          Future.successful(Seq())
        }
      }
    }
  }

  override def getMyPostTimelineFeeds(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[TimelineFeed]] = {
    FutureUtils.timedFuture("getMyPostTimelineFeeds") {
      val results = if (timestamp.isDefined) {
        db.run(posts.filter(p => p.userId === userId && p.created < timestamp.get).sortBy(_.created.desc).map(p => (p.id, p.created)).take(pageSize).result)
      } else {
        db.run(posts.filter(_.userId === userId).sortBy(_.created.desc).map(p => (p.id, p.created)).take(pageSize).result)
      }
      results.map { rows =>
        rows.map(row => TimelineFeed(row._1, TimelineFeed.TypeMyPost, ts = row._2))
      }
    }
  }

  override def getEditorPickTimelineFeeds(pageSize: Int, timestamp: Option[Long]): Future[Seq[TimelineFeed]] = {
    FutureUtils.timedFuture("getEditorPickTimelineFeeds") {
      val results = if (timestamp.isDefined) {
        val query = sql"""SELECT post_id, created FROM recommend WHERE created < ${timestamp.get} ORDER BY created DESC LIMIT $pageSize""".as[(Long, Long)]
        db.run(query)
      } else {
        val query = sql"""SELECT post_id, created FROM recommend ORDER BY created DESC LIMIT $pageSize""".as[(Long, Long)]
        db.run(query)
      }
      results.map { rows =>
        rows.map(row => TimelineFeed(row._1, TimelineFeed.TypeEditorPick, ts = row._2))
      }
    }
  }

  override def getBasedOnTagTimelineFeeds(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[TimelineFeed]] = {
    FutureUtils.timedFuture("getBasedOnTagTimelineFeeds") {
      db.run(userTags.filter(t => t.userId === userId && t.subscribe === true).map(_.tagId).result).flatMap { tagIds =>

        val randomTagIds = Random.shuffle(tagIds).take(500).mkString(",")

        if (randomTagIds.nonEmpty) {
          val query = if (timestamp.isDefined) {
            sql"""SELECT post_id, user_id, tag, created FROM mark WHERE created < ${timestamp.get} AND tag_id IN (#${randomTagIds}) order by created desc limit ${pageSize * 4}""".as[(Long, Long, String, Long)]
          } else {
            sql"""SELECT post_id, user_id, tag, created FROM mark WHERE tag_id IN (#${randomTagIds}) order by created desc limit ${pageSize * 4}""".as[(Long, Long, String, Long)]
          }
          db.run(query).map { results =>
            results
              .groupBy(_._2).map(_._2.head).toSeq
              .sortBy(x => x._1)
              .reverse
              .take(pageSize)
              .map(row => TimelineFeed(row._1, TimelineFeed.TypeBasedOnTag, Some(row._3), ts = row._4))
          }
        } else {
          Future.successful(Seq())
        }
      }
    }
  }

  override def getUserTags(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Set[String]] = {
    FutureUtils.timedFuture("getUserTags") {
      val tagsQuery = sql"""select tag from tag where id in (select tag_id from user_tags where user_id=$userId and subscribe=true)""".as[String]
      db.run(tagsQuery).map(_.toSet)
    }
  }

  override def getRecentPosts(pageSize: Int, timestamp: Option[Long], filter: Option[String]): Future[Seq[Long]] = {
    if (filter.isDefined && filter.get.length > 0) {
      val name = filter.get

      val jian = JianFan.f2j(name).toLowerCase
      val fan = JianFan.j2f(name).toLowerCase

      if (timestamp.isDefined) {
        val query = sql"""SELECT DISTINCT p.id FROM post p INNER JOIN mark m ON p.id=m.post_id INNER JOIN tag t ON m.tag_id=t.id WHERE (t.tag like '%#${jian}%' OR t.tag like '%#${fan}%') AND p.created < ${timestamp.get} order by p.created desc limit $pageSize""".as[Long]
        db.run(query)
      } else {
        val query = sql"""SELECT DISTINCT p.id FROM post p INNER JOIN mark m ON p.id=m.post_id INNER JOIN tag t ON m.tag_id=t.id WHERE t.tag like '%#${jian}%' OR t.tag like '%#${fan}%' order by p.created desc limit $pageSize""".as[Long]
        db.run(query)
      }
    } else {
      if (timestamp.isDefined) {
        db.run(posts.filter(_.created < timestamp.get).sortBy(_.created.desc).take(pageSize).map(_.id).result)
      } else {
        db.run(posts.sortBy(_.created.desc).take(pageSize).map(_.id).result)
      }
    }
  }

  override def getRecentPostsForUser(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Seq[Long]] = {
    if (timestamp.isDefined) {
      db.run(posts.filter(p => p.created < timestamp.get && p.userId === userId).sortBy(_.created.desc).take(pageSize).map(_.id).result)
    } else {
      db.run(posts.filter(_.userId === userId).sortBy(_.created.desc).take(pageSize).map(_.id).result)
    }
  }

  override def getFavoritePostsForUser(userId: Long, pageSize: Int, timestamp: Option[Long]): Future[Map[Long, Long]] = {
    if (timestamp.isDefined) {
      db.run(favorites.filter(fav => fav.created < timestamp.get && fav.userId === userId).sortBy(_.created.desc).take(pageSize).result).map(_.map(fav => (fav.postId, fav.created)).toMap)
    } else {
      db.run(favorites.filter(_.userId === userId).sortBy(_.created.desc).take(pageSize).result).map(_.map(fav => (fav.postId, fav.created)).toMap)
    }
  }

  override def recordDelete(photo: String): Future[Unit] = {
    db.run(deletes += DeletedPhoto(None, photo)).map(_ => ())
  }

  override def getPersonalizedPostsForUser(userId: Long, ratio: Double, pageSize: Int, timestamp: Option[Long]): Seq[Long] = {
    try {
      RedisCacheClient.hget(KeyUtils.userCategory, userId.toString).map(_.split(",").toSeq.map(_.toDouble)).getOrElse(Seq()).zipWithIndex.map {
        case (num, i) =>
          val n = scala.math.ceil(num * ratio).toInt
          if (n > 0) {
            // get posts with least view
            val ids = if (timestamp.isDefined) {
              RedisCacheClient.zrevrangebyscore(KeyUtils.category(i), 0, timestamp.get - 1, 0, n)
            } else {
              RedisCacheClient.zrevrangebyscore(KeyUtils.category(i), 0, Long.MaxValue, 0, n)
            }
            ids.map(_.toLong)
          } else {
            Set[Long]()
          }
      }.flatMap(x => x).sortWith(_ > _)
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        Seq()
    }
  }

  override def getReports(pageSize: Int, timestamp: Option[Long]): Future[Seq[Report]] = {
    if (timestamp.isDefined) {
      db.run(reports.filter(_.created < timestamp).take(pageSize).result)
    } else {
      db.run(reports.take(pageSize).result)
    }
  }

  override def get30DayHotUsers(num: Int): Future[Seq[User]] = {
    val cachedUsers = RedisCacheClient.srandmember(KeyUtils.hotUsers, 15)
    if (cachedUsers.isEmpty) {
      val ts30ago = DateTime.now().minusDays(30).getMillis / 1000
      val query = posts.filter(_.created > ts30ago)
        .groupBy(_.userId)
        .map(x => (x._1, x._2.size))
        .sortBy(_._2.desc)
        .take(100)
      db.run(query.result).flatMap { pool =>
        RedisCacheClient.sadd(KeyUtils.hotUsers, pool.map(_._1.toString))
        val userIds = Random.shuffle(pool).take(num).map(_._1)
        db.run(users.filter(u => (u.id inSet userIds) && u.avatar =!= "default_avatar.jpg").result)
      }
    } else {
      db.run(users.filter(u => (u.id inSet cachedUsers.map(_.toLong)) && u.avatar =!= "default_avatar.jpg").result)
    }
  }

  override def get7DayHotUsers(num: Int): Future[Seq[User]] = {
    val ts7ago = DateTime.now().minusDays(7).getMillis / 1000
    val query = (for {
      (post, user) <- posts join users on (_.userId === _.id)
      if user.created > ts7ago && user.avatar =!= "default_avatar.jpg"
    } yield user).groupBy(_.id).map(x => (x._1, x._2.size))
      .sortBy(_._2.desc)
      .take(30)

    db.run(query.result).flatMap { pool =>
      println(pool)
      val userIds = Random.shuffle(pool).take(num).map(_._1)
      println(userIds)
      db.run(users.filter(_.id inSet userIds).result)
    }
  }

}
