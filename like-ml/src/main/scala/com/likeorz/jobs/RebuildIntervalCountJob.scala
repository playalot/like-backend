package com.likeorz.jobs

import java.sql.Connection

import com.likeorz.mllib.utils.{ KeyUtils, RedisUtils, MysqlUtils }
import com.typesafe.config.ConfigFactory

object RebuildIntervalCountJob {

  val conf = ConfigFactory.load()

  def rebuildTagUsageCountCache(): Unit = {
    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null
    var connection3: Connection = null

    println("Rebuild tag usage counts start!")
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

      val resultSet = statement.executeQuery("select id from tag")
      while (resultSet.next()) {
        val tagId = resultSet.getLong("id")

        val rs = subquery.executeQuery("select count(*) from mark where tag_id=" + tagId)
        while (rs.next()) {
          val usageCount = rs.getLong(1)
          updatequery.executeUpdate("update tag set `usage` = " + usageCount + " where id=" + tagId)
          RedisUtils.zrem(KeyUtils.tagUsage, tagId.toString)
        }

        count += 1
        if (count % 2000 == 0) {
          println("2000 tags processed...")
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

  def incrementalUpdateTagUsageCount(): Unit = {

    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null

    println("Rebuild tag usage counts start!")
    val processStart = System.nanoTime()
    try {
      // make the connection
      connection = MysqlUtils.getConnection(conf)
      connection2 = MysqlUtils.getConnection(conf)

      // create the statement, and run the select query
      val statement = connection.createStatement()
      val updatequery = connection2.createStatement()

      var count = 0
      RedisUtils.zrevrangeWithScores(KeyUtils.tagUsage, 0, 10000).foreach { kv =>
        count += 1
        val (tagId, value) = kv
        RedisUtils.zrem(KeyUtils.tagUsage, tagId)

        val resultSet = statement.executeQuery("select `usage` from tag where id=" + tagId)
        while (resultSet.next()) {
          val usage = resultSet.getLong("usage")
          updatequery.executeUpdate("update tag set `usage`= " + (usage + value.toLong) + " where id=" + tagId)
        }
      }

      println("Update " + count + " tags..")
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    connection.close()
    connection2.close()
    val processElapsed = (System.nanoTime() - processStart) / 1e9
    println(s"Processing time: $processElapsed sec")
  }

}
