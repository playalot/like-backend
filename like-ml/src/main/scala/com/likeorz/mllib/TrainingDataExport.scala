package com.likeorz.mllib

import java.sql.DriverManager
import java.sql.Connection

import com.typesafe.config.ConfigFactory

object TrainingDataExport {

  val conf = ConfigFactory.load()

  def exportPostTags(): Unit = {

    val PREFIX = conf.getString("train.prefix")

    // connect to the database
    val driver = conf.getString("mysql.driver")
    val url = conf.getString("mysql.url")
    val username = conf.getString("mysql.user")
    val password = conf.getString("mysql.password")

    // there's probably a better way to do this
    var connection: Connection = null
    var connection2: Connection = null
    val p = new java.io.PrintWriter(new java.io.File(s"$PREFIX/post_tags.csv"))

    println("Export post tags start!")
    val preprocessStart = System.nanoTime()

    try {
      // make the connection
      Class.forName(driver)
      connection = DriverManager.getConnection(url, username, password)
      connection2 = DriverManager.getConnection(url, username, password)

      // create the statement, and run the select query
      val statement = connection.createStatement()
      val subquery = connection2.createStatement()
      val resultSet = statement.executeQuery("select id, user_id from post")
      while (resultSet.next()) {
        val postId = resultSet.getString("id")
        val userId = resultSet.getString("user_id")

        val rs = subquery.executeQuery("select t.tag from mark m inner join tag t on m.tag_id = t.id  where m.post_id=" + postId)

        val tags = scala.collection.mutable.ArrayBuffer[String]()
        while (rs.next()) {
          tags.append(rs.getString(1))
        }
        p.println(postId + "," + userId + "," + tags.mkString(","))
      }
    } catch {
      case e: Throwable => e.printStackTrace()
    }
    p.close()
    connection.close()
    connection2.close()
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
    println(s"Preprocessing time: $preprocessElapsed sec")

  }

}

