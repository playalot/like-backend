package com.likeorz.jobs

import java.sql.Connection

import com.likeorz.mllib.utils.{ KeyUtils, RedisUtils, MysqlUtils }
import com.typesafe.config.ConfigFactory
import scalikejdbc._
import scalikejdbc.config._

import scalikejdbc.AutoSession

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

object RebuildCacheJob {

  val conf = ConfigFactory.load()

  def rebuildUserCountsCache(): Unit = {

    DBs.setupAll()

    implicit val session = AutoSession

    println("Rebuild user counts start!")
    val preprocessStart = System.nanoTime()
    try {

      var count = 0

      sql"select id, nickname, avatar, cover from user".foreach { rs =>
        val userId = rs.long("id")
        val nickname = rs.string("nickname")
        val avatar = rs.string("avatar")
        val cover = rs.string("cover")
        // Update user cache
        RedisUtils.hmset(KeyUtils.user(userId), Map[String, String](
          "nickname" -> nickname,
          "avatar" -> avatar,
          "cover" -> cover))

        val postCount = sql"select count(*) from post where user_id=${userId}".map(_.long(1)).single.apply().getOrElse(0)
        RedisUtils.hset(KeyUtils.user(userId), "posts", postCount.toString)

        val followingCount = sql"select count(*) from follow where from_id=${userId}".map(_.long(1)).single.apply().getOrElse(0)
        RedisUtils.hset(KeyUtils.user(userId), "following", followingCount.toString)

        val followerCount = sql"select count(*) from follow where to_id=${userId}".map(_.long(1)).single.apply().getOrElse(0)
        RedisUtils.hset(KeyUtils.user(userId), "followers", followerCount.toString)

        val favoritesCount = sql"select count(*) from favorite where user_id=${userId}".map(_.long(1)).single.apply().getOrElse(0)
        RedisUtils.hset(KeyUtils.user(userId), "favorites", favoritesCount.toString)

        count += 1
        if (count % 2000 == 0) {
          println("2000 users processed...")
        }
      }

    } catch {
      case e: Throwable => e.printStackTrace()
    }

    val processElapsed = (System.nanoTime() - preprocessStart) / 1e9
    println(s"Processing time: $processElapsed sec")
  }

  def rebuildMarkCache(): Unit = {
    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null

    println("Rebuild posts marks cache start!")
    val preprocessStart = System.nanoTime()
    var count = 0
    try {
      // make the connection
      connection = MysqlUtils.getConnection(conf)
      connection2 = MysqlUtils.getConnection(conf)

      // create the statement, and run the select query
      val statement = connection.createStatement()

      val querySQL = "select m.id, count(1) from `like` l inner join mark m on l.mark_id=m.id where m.post_id = ? group by m.id"
      val subquery = connection2.prepareStatement(querySQL)

      val resultSet = statement.executeQuery("select id from post order by id desc")
      while (resultSet.next()) {
        val postId = resultSet.getLong("id")
        print(postId + ",")
        subquery.setLong(1, postId)
        val rs = subquery.executeQuery()
        while (rs.next()) {
          val markId = rs.getLong(1)
          val count = rs.getLong(2)
          RedisUtils.zadd(KeyUtils.postMark(postId), count, markId.toString)
        }
      }
      count += 1
      if (count % 200 == 0) println("Processed " + count)
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    connection.close()
    connection2.close()
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
    println(s"Preprocessing time: $preprocessElapsed sec")
  }

  def rebuildUserLikes(): Unit = {

    DBs.setupAll()

    implicit val session = AutoSession

    println("Rebuild database user likes start!")

    try {

      var count = 0

      val ids = sql"select id from user where id < 12000".map(_.long("id")).list.apply()

      val futures = ids.grouped(3000).toList.map { x =>
        Future {
          println("start a thread")
          x.foreach { userId =>
            val likes = sql"select count(*) from `like` l inner join mark m on l.mark_id=m.id inner join post p on m.post_id=p.id where m.user_id=${userId} or p.user_id=${userId}".map(_.long(1)).single.apply().getOrElse(0)
            RedisUtils.hset(KeyUtils.user(userId), "likes", likes.toString)
            sql"update user set likes=${likes} where id=${userId}".update.apply()
            println(userId + ":" + likes)
            count += 1
            if (count % 200 == 0) println("Processed " + count)
          }
        }
      }

      val processStart = System.nanoTime()
      Await.ready(Future.sequence(futures), Duration.Inf)
      println("all threads completed")
      val processElapsed = (System.nanoTime() - processStart) / 1e9
      println(s"Preprocessing time: $processElapsed sec")

      //      sql"select id from user limit 100".foreach { rs =>
      //        val userId = rs.long("id")
      //
      //        val likes = sql"select count(*) from `like` l inner join mark m on l.mark_id=m.id inner join post p on m.post_id=p.id where m.user_id=${userId} or p.user_id=${userId}".map(_.long(1)).single.apply().getOrElse(0)
      //        RedisUtils.hset(KeyUtils.user(userId), "likes", likes.toString)
      //        sql"update user set likes=${likes} where id=${userId}".update.apply()
      //
      //        count += 1
      //        if (count % 200 == 0) println("Processed " + count)
      //      }

    } catch {
      case e: Throwable => e.printStackTrace()
    }

  }

  def updateDBUserLikes(): Unit = {
    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null
    var connection3: Connection = null

    println("Update database user likes start!")
    val preprocessStart = System.nanoTime()
    try {
      // make the connection
      connection = MysqlUtils.getConnection(conf)
      connection2 = MysqlUtils.getConnection(conf)
      connection3 = MysqlUtils.getConnection(conf)

      // create the statement, and run the select query
      val statement = connection.createStatement()
      val subquery = connection2.createStatement()
      val updatequery = connection3.createStatement()

      var count = 0

      val resultSet = statement.executeQuery("select id, likes from user")
      while (resultSet.next()) {
        val userId = resultSet.getLong("id")
        val likes = resultSet.getLong("likes")
        // Update user cache
        val cachedLikes = RedisUtils.hget(KeyUtils.user(userId), "likes").map(_.toLong).getOrElse(0L)

        if (cachedLikes > likes) {
          println(s"User[$userId] Cache $cachedLikes -> $likes")
          updatequery.executeUpdate("update user set likes = " + cachedLikes + " where id=" + userId)
        } else if (cachedLikes == 0) {
          val rs = subquery.executeQuery("select count(*) from `like` l inner join mark m on l.mark_id=m.id where m.user_id=" + userId)
          while (rs.next()) {
            val likeCount = rs.getLong(1)
            RedisUtils.hset(KeyUtils.user(userId), "likes", likeCount.toString)
            updatequery.executeUpdate("update user set likes = " + likeCount + " where id=" + userId)
          }

          while (rs.next()) {
            val likeCount = rs.getLong(1)
            if (likeCount != 0) {
              println(s"User[$userId] Compute $likeCount -> $likes")
              count += 1
              RedisUtils.hset(KeyUtils.user(userId), "likes", likeCount.toString)
              updatequery.executeUpdate("update user set likes = " + cachedLikes + " where id=" + userId)
            }
          }
        }
      }
      println(s"Total re-compute $count user likes")
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    connection.close()
    connection2.close()
    connection3.close()
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
    println(s"Preprocessing time: $preprocessElapsed sec")
  }

}
