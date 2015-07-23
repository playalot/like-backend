package com.likeorz.mllib.utils

import java.sql.{ Connection, DriverManager }

import com.typesafe.config.Config

object MysqlUtils {

  def getConnection(conf: Config): Connection = {
    // connect to the database
    val driver = conf.getString("mysql.driver")
    val url = conf.getString("mysql.url")
    val username = conf.getString("mysql.user")
    val password = conf.getString("mysql.password")
    Class.forName(driver)
    DriverManager.getConnection(url, username, password)
  }

}
