package com.likeorz.services

import javax.inject.{ Inject, Singleton }

import com.likeorz.event.LikeEvent
import com.likeorz.models.TimelineFeed
import com.mongodb.ServerAddress
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import play.api.Configuration
import play.api.libs.json.Json

@Singleton
class MongoDBService @Inject() (configuration: Configuration) {

  val hostname = configuration.getString("mongodb.hostname").getOrElse("localhost")
  val username = configuration.getString("mongodb.username").getOrElse("like_api")
  val password = configuration.getString("mongodb.password").getOrElse("like_api")
  val database = configuration.getString("mongodb.database").getOrElse("like_api")

  val server = new ServerAddress(hostname, 27017)

  val credentials = MongoCredential.createCredential(username, database, password.toCharArray)

  private lazy val mongoClient = MongoClient(server, List(credentials))

  private lazy val db = mongoClient(database)

  def insertEvent(event: LikeEvent) = {
    db("events").insert(JSON.parse(Json.toJson(event).toString()).asInstanceOf[DBObject])
  }

  def insertTimelineFeedForUser(timelineFeed: TimelineFeed, userId: Long, limit: Int = 1000) = {
    val query = MongoDBObject("_id" -> userId)
    val update = MongoDBObject(
      "$push" -> MongoDBObject(
        "feeds" -> MongoDBObject(
          "$each" -> MongoDBList(TimelineFeed.toDBObject(timelineFeed)),
          "$sort" -> MongoDBObject("ts" -> -1),
          "$slice" -> -limit
        )
      )
    )
    db("timeline").update(query, update, upsert = true)
  }

  def postInTimelineForUser(postId: Long, userId: Long): Boolean = {
    db("timeline").findOne(MongoDBObject("_id" -> userId, "feeds.postId" -> postId)).isDefined
  }

  def getFeedsFromTimelineForUser(userId: Long, limit: Int = 10, anchor: Option[Long] = None): Seq[TimelineFeed] = {
    if (anchor.isEmpty) {
      db("timeline")
        .findOne(MongoDBObject("_id" -> userId), MongoDBObject("feeds" -> MongoDBObject("$slice" -> limit))) match {
          case Some(rows) =>
            rows.as[MongoDBList]("feeds").map { x =>
              TimelineFeed.fromDBObject(x.asInstanceOf[BasicDBObject])
            }
          case None => Seq()
        }
    } else {
      db("timeline")
        .findOne(MongoDBObject("_id" -> userId)) match {
          case Some(rows) =>
            rows.as[MongoDBList]("feeds").map { x =>
              TimelineFeed.fromDBObject(x.asInstanceOf[BasicDBObject])
            }.filter(_.ts < anchor.get).take(limit)
          case None => Seq()
        }
    }
  }

}
