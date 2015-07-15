package com.likeorz.cluster.actors

import akka.actor.{ Actor, ActorLogging, Props }
import com.likeorz.cluster.utils.MLUtils
import com.likeorz.common.{ Tag, Tags }
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.Word2VecModel

class ClassifyActor(w2v: Word2VecModel, cluster: KMeansModel) extends Actor with ActorLogging {

  override def receive = {
    case Tags(tags) =>
      val num = cluster.predict(MLUtils.wordsToVector(tags.flatMap(tag => MLUtils.cleanTag(tag)), w2v))
      sender() ! num
    case Tag(tag) =>
      val synonyms = w2v.findSynonyms(tag, 10).map(_._1).toList
      sender() ! synonyms
    case _ =>
      println("Invalid message!")
  }
}

object ClassifyActor {
  def props(w2v: Word2VecModel, cluster: KMeansModel) = Props(classOf[ClassifyActor], w2v, cluster)
}