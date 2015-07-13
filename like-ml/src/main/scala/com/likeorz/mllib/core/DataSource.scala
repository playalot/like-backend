package com.likeorz.mllib.core

import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD

/**
 * Created by Guan Guan
 * Date: 7/6/15
 */
class DataSource extends Serializable {

  def readTraining(sc: SparkContext): TrainingData = {
    val usersRDD = sc.textFile("likes.csv")
      .map(_.split(",")(0))
      .distinct()
      .map(id => (id, User()))
      .cache()

    val postsRDD = sc.textFile("post_tags.csv").map { line =>
      val postId = line.split(",")(0)
      val tags = line.split(",").drop(1).filter(_.trim.length > 0).toList
      if (tags.size > 0) (postId, Post(Some(tags)))
      else (postId, Post(None))
    }.cache()

    val likeEventsRDD = sc.textFile("likes.csv").map { line =>
      val fields = line.split(",")
      LikeEvent(fields(0), fields(1), fields(3).toLong)
    }

    new TrainingData(
      users = usersRDD,
      posts = postsRDD,
      likeEvents = likeEventsRDD
    )
  }
}

case class User()

case class Post(tags: Option[List[String]])

case class LikeEvent(user: String, item: String, t: Long)

case class MarkEvent(user: String, item: String, t: Long)

class TrainingData(
    val users: RDD[(String, User)],
    val posts: RDD[(String, Post)],
    val likeEvents: RDD[LikeEvent]) extends Serializable {
  override def toString = {
    s"users: [${users.count()} (${users.take(2).toList}...)]" +
      s"posts: [${posts.count()} (${posts.take(2).toList}...)]" +
      s"likeEvents: [${likeEvents.count()}] (${likeEvents.take(2).toList}...)"
  }
}
