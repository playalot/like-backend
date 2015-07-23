package com.likeorz.jobs

import java.sql.Connection

import com.likeorz.mllib.utils.{ KeyUtils, RedisUtils, MysqlUtils }
import com.typesafe.config.ConfigFactory

object RebuildCacheJob {

  val conf = ConfigFactory.load()

  def rebuildUserCountsCache(): Unit = {
    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null
    var connection3: Connection = null

    println("Rebuild user counts start!")
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

      val resultSet = statement.executeQuery("select id, nickname, avatar, cover from user")
      while (resultSet.next()) {
        val userId = resultSet.getLong("id")
        val nickname = resultSet.getString("nickname")
        val avatar = resultSet.getString("avatar")
        val cover = resultSet.getString("cover")

        // Update user cache
        RedisUtils.hmset(KeyUtils.user(userId), Map[String, String](
          "nickname" -> nickname,
          "avatar" -> avatar,
          "cover" -> cover))

        var rs = subquery.executeQuery("select count(*) from post where user_id=" + userId)
        while (rs.next()) {
          val postCount = rs.getLong(1)
          RedisUtils.hset(KeyUtils.user(userId), "posts", postCount.toString)
        }
        rs = subquery.executeQuery("select count(*) from follow where from_id=" + userId)
        while (rs.next()) {
          val followingCount = rs.getLong(1)
          RedisUtils.hset(KeyUtils.user(userId), "followings", followingCount.toString)
        }
        rs = subquery.executeQuery("select count(*) from follow where to_id=" + userId)
        while (rs.next()) {
          val followerCount = rs.getLong(1)
          RedisUtils.hset(KeyUtils.user(userId), "followers", followerCount.toString)
        }
        rs = subquery.executeQuery("select count(*) from `like` l inner join mark m on l.mark_id=m.id where m.user_id=" + userId)
        while (rs.next()) {
          val likeCount = rs.getLong(1)
          RedisUtils.hset(KeyUtils.user(userId), "likes", likeCount.toString)
          updatequery.executeUpdate("update user set likes = " + likeCount + " where id=" + userId)
        }

      }
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    connection.close()
    connection2.close()
    connection3.close()
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
    println(s"Preprocessing time: $preprocessElapsed sec")
  }

  def rebuildMarkCache(): Unit = {
    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null

    println("Rebuild posts marks cache start!")
    val preprocessStart = System.nanoTime()
    try {
      // make the connection
      connection = MysqlUtils.getConnection(conf)
      connection2 = MysqlUtils.getConnection(conf)

      // create the statement, and run the select query
      val statement = connection.createStatement()
      val subquery = connection2.createStatement()

      val resultSet = statement.executeQuery("select id from post")
      while (resultSet.next()) {
        val postId = resultSet.getLong("id")

        var rs = subquery.executeQuery(s"select m.id, count(1) from `like` l inner join mark m on l.mark_id=m.id where m.post_id=$postId group by m.id")
        while (rs.next()) {
          val markId = rs.getLong(1)
          val count = rs.getLong(2)
          RedisUtils.zadd(KeyUtils.postMark(postId), count, markId.toString)
        }
      }
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    connection.close()
    connection2.close()
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
    println(s"Preprocessing time: $preprocessElapsed sec")
  }
}
