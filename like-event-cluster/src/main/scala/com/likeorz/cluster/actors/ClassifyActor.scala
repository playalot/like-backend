package com.likeorz.cluster.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.likeorz.cluster.utils.{ RedisCacheClient, MLUtils }
import com.likeorz.common.{ ClassifyPost, Tag, Tags }
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.Word2VecModel

class ClassifyActor(w2v: Word2VecModel, cluster: KMeansModel) extends Actor with ActorLogging {

  override def receive = {
    case ClassifyPost(id, tags, timestamp) =>
      val num = cluster.predict(MLUtils.wordsToVector(tags.flatMap(tag => MLUtils.cleanTag(tag)), w2v))
      RedisCacheClient.zadd("category:" + num, timestamp, id.toString)
      log.info(s"Post[$id](${tags.mkString(",")}}) -> category[$num]")
    case Tags(tags) =>
      val num = cluster.predict(MLUtils.wordsToVector(tags.flatMap(tag => MLUtils.cleanTag(tag)), w2v))
      sender() ! num
    case Tag(tag) =>
      val synonyms = try {
        w2v.findSynonyms(tag, 10).map(_._1).toList
      } catch {
        case e: Throwable => List[String]()
      }
      sender() ! synonyms
    case _ =>
      println("Invalid message!")
  }
}

object ClassifyActor {
  def props(w2v: Word2VecModel, cluster: KMeansModel) = Props(classOf[ClassifyActor], w2v, cluster)
}