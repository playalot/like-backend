package com.likeorz.cluster.utils

import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON

object MongoDB {

  val server = new ServerAddress("localhost", 27017)

  val credentials = MongoCredential.createCredential("likeorz", "likeorz", "LikeMG2015".toCharArray)

  private lazy val mongoClient = MongoClient(server, List(credentials))

  private lazy val db = mongoClient("likeorz")

  private lazy val collection = db("events")

  def insertEventJson(json: String) = {
    collection.insert(JSON.parse(json).asInstanceOf[DBObject])
  }

}
