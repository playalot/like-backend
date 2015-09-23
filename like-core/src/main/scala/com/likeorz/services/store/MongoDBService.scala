package com.likeorz.services.store

import javax.inject.{ Inject, Singleton }

import com.likeorz.event.LikeEvent
import com.likeorz.models.{ MarkDetail, PostMarks, TimelineFeed }
import com.mongodb.ServerAddress
import com.mongodb.casbah.Imports._
import com.mongodb.util.JSON
import play.api.Configuration
import play.api.libs.json.Json

@Singleton
class MongoDBService @Inject() (configuration: Configuration) {

  private val hostname = configuration.getString("mongodb.hostname").get
  private val username = configuration.getString("mongodb.username").get
  private val password = configuration.getString("mongodb.password").get
  private val database = configuration.getString("mongodb.database").get

  private val server = new ServerAddress(hostname, 27017)

  private val credentials = MongoCredential.createCredential(username, database, password.toCharArray)

  private val mongoClient = MongoClient(server, List(credentials))

  private val db = mongoClient(database)

  def insertPostMarks(postMarks: PostMarks) = {
    try {
      db("posts").insert(PostMarks.toDBObject(postMarks))
    } catch {
      case _: Throwable =>
    }
  }

  def deletePostMarks(postId: Long) = {
    db("posts").findAndRemove(MongoDBObject("_id" -> postId))
  }

  def insertMarkForPost(postId: Long, markDetail: MarkDetail) = {
    if (db("posts").findOne(MongoDBObject("_id" -> postId, "marks.m_id" -> markDetail.markId)).isEmpty) {
      val query = MongoDBObject("_id" -> postId)
      val update = MongoDBObject(
        "$push" -> MongoDBObject("marks" -> MarkDetail.toDBObject(markDetail))
      )
      db("posts").update(query, update)
    }
  }

  def deleteMarkForPost(markId: Long, postId: Long) = {
    val query = MongoDBObject("_id" -> postId)
    val update = MongoDBObject(
      "$pull" -> MongoDBObject(
        "marks" -> MongoDBObject("m_id" -> markId)
      )
    )
    println(update)
    db("posts").update(query, update)
  }

  def likeMark(markId: Long, postId: Long, userId: Long) = {
    val query = MongoDBObject("_id" -> postId, "marks.m_id" -> markId)
    val update = MongoDBObject(
      "$addToSet" -> MongoDBObject("marks.$.likes" -> userId)
    )
    db("posts").update(query, update)
  }

  def unlikeMark(markId: Long, postId: Long, userId: Long) = {
    val query = MongoDBObject("_id" -> postId, "marks.m_id" -> markId)
    val update = MongoDBObject(
      "$pull" -> MongoDBObject("marks.$.likes" -> userId)
    )
    db("posts").update(query, update)
  }

  def findPostsByTags(tagIds: Seq[Long]): Seq[PostMarks] = {
    db("posts").find(MongoDBObject("marks.t_id" -> MongoDBObject("$in" -> tagIds))).toSeq.map { row =>
      PostMarks.fromDBObject(row)
    }
  }

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
          "$slice" -> limit
        )
      )
    )
    db("timeline").update(query, update, upsert = true)
  }

  def removeTimelineFeedByPostId(postId: Long) = {
    val update = MongoDBObject(
      "$pull" -> MongoDBObject(
        "feeds" -> MongoDBObject("postId" -> postId)
      )
    )
    db("timeline").update(MongoDBObject.empty, update, multi = true)
  }

  def removeTimelineFeedForUserWhenMarkRemoved(userId: Long, postId: Long, tag: String) = {
    val query = MongoDBObject("_id" -> userId)
    val update = MongoDBObject(
      "$pull" -> MongoDBObject(
        "feeds" -> MongoDBObject("postId" -> postId, "tag" -> tag)
      )
    )
    db("timeline").update(query, update)
  }

  def removeTimelineFeedForUserWhenUnsubscribeTag(userId: Long, tag: String) = {
    val query = MongoDBObject("_id" -> userId)
    val update = MongoDBObject(
      "$pull" -> MongoDBObject(
        "feeds" -> MongoDBObject("tag" -> tag)
      )
    )
    db("timeline").update(query, update, multi = true)
  }

  def removeTimelineFeedsForUserByTag(userId: Long, tag: String) = {
    val query = MongoDBObject("_id" -> userId)
    val update = MongoDBObject(
      "$pull" -> MongoDBObject(
        "feeds" -> MongoDBObject("tag" -> tag)
      )
    )
    db("timeline").update(query, update, multi = true)
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

  def getPostMarksByIds(ids: Seq[Long]): Map[Long, Seq[MarkDetail]] = {
    db("posts").find("_id" $in ids).map { row =>
      val post = PostMarks.fromDBObject(row)
      post.postId -> post.marks.sortBy(_.likes.size).reverse
    }.toMap
  }

  def findPostsByTagIds(ids: Seq[Long], pageSize: Int, timestamp: Option[Long]): Seq[TimelineFeed] = {
    val query = if (timestamp.isDefined) {
      ("marks.t_id" $in ids) ++ ("ts" $lt timestamp.get)
    } else {
      "marks.t_id" $in ids
    }
    val order = MongoDBObject("ts" -> -1)
    db("posts").find(query).sort(order).limit(pageSize).toList.map { row =>
      val postMarks = PostMarks.fromDBObject(row)
      val tag = postMarks.marks.find(m => ids.contains(m.tagId)).map(_.tag)
      TimelineFeed(postMarks.postId, TimelineFeed.TypeBasedOnTag, tag, ts = postMarks.ts)
    }
  }

}
