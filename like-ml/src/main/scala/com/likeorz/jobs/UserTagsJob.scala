package com.likeorz.jobs

import java.sql.Connection

import com.likeorz.mllib.utils.{ KeyUtils, RedisUtils, MysqlUtils }
import com.typesafe.config.ConfigFactory

object UserTagsJob {

  val conf = ConfigFactory.load()

  def populateUserTags(): Unit = {
    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null
    var connection3: Connection = null

    println("Populate user tags start!")
    val processStart = System.nanoTime()
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

      val resultSet = statement.executeQuery("select id from user")
      while (resultSet.next()) {
        val userId = resultSet.getLong("id")

        val rs = subquery.executeQuery("select distinct m.tag_id, t.group from mark m inner join tag t on m.tag_id=t.id where  t.group>0 and m.user_id=" + userId)
        while (rs.next()) {
          val tagId = rs.getLong(1)
          val tagGroup = rs.getInt(2)
          if (tagGroup > 0) {
            try {
              updatequery.executeUpdate(s"insert into user_tags values ($userId, $tagId, 1)")
            } catch {
              case e: Throwable => ()
            }
          }
        }

        count += 1
        if (count % 2000 == 0) {
          println("2000 users processed...")
        }
      }
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    connection.close()
    connection2.close()
    connection3.close()
    val processElapsed = (System.nanoTime() - processStart) / 1e9
    println(s"Processing time: $processElapsed sec")
  }

  def addTagForAllUser(tagId: Long): Unit = {
    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null
    var connection3: Connection = null

    println("Add tag to user start!")
    val processStart = System.nanoTime()
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

      val resultSet = statement.executeQuery("select id from user")
      while (resultSet.next()) {
        val userId = resultSet.getLong("id")

        val markResult = subquery.executeQuery("select user_id from mark where user_id=" + userId + " and tag_id=28 limit 1")

        while (markResult.next()) {
          try {
            updatequery.executeUpdate(s"insert into user_tags values ($userId, $tagId, 1)")
          } catch {
            case e: Throwable => ()
          }
        }

        count += 1
        if (count % 2000 == 0) {
          println("2000 users processed...")
        }
      }
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    connection.close()
    connection3.close()
    val processElapsed = (System.nanoTime() - processStart) / 1e9
    println(s"Processing time: $processElapsed sec")
  }

}
