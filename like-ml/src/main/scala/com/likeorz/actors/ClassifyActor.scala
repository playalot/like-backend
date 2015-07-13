package com.likeorz.actors

import akka.actor.{ Props, ActorLogging, Actor }
import com.likeorz.mllib.utils.MLUtils
import org.apache.spark.mllib.feature.Word2VecModel
import org.apache.spark.mllib.clustering.KMeansModel

class ClassifyActor(w2v: Word2VecModel, cluster: KMeansModel) extends Actor with ActorLogging {
  import com.likeorz.actors.ClassifyActor._

  override def receive = {
    case Tags(tags) =>
      val num = cluster.predict(MLUtils.wordsToVector(tags.flatMap(tag => MLUtils.cleanTag(tag)), w2v))
      sender() ! num
    case _ =>
      println("Invalid message!")
  }

}

object ClassifyActor {
  def props(w2v: Word2VecModel, cluster: KMeansModel) = Props(classOf[ClassifyActor], w2v, cluster)
  case class Tags(tags: Seq[String])
}