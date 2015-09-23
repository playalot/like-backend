package com.likeorz.mllib.utils

import com.likeorz.models.PostMarks
import com.mongodb.ServerAddress
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import com.typesafe.config.ConfigFactory

object MongoDB {

  private val configuration = ConfigFactory.load()

  private val hostname = configuration.getString("mongodb.hostname")
  private val username = configuration.getString("mongodb.username")
  private val password = configuration.getString("mongodb.password")
  private val database = configuration.getString("mongodb.database")

  private val server = new ServerAddress(hostname, 27017)

  private val credentials = MongoCredential.createCredential(username, database, password.toCharArray)

  private lazy val mongoClient = MongoClient(server, List(credentials))

  private lazy val db = mongoClient(database)

  def insertEventJson(json: String) = {
    db("events").insert(JSON.parse(json).asInstanceOf[DBObject])
  }

  def insertPostMarks(postMarks: PostMarks) = {
    db("posts").insert(PostMarks.toDBObject(postMarks))
  }

}
