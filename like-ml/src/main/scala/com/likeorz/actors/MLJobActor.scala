package com.likeorz.actors

import akka.actor.{ Props, ActorLogging, Actor }
import com.likeorz.LikeMLApp
import com.likeorz.mllib.KMeansOnTags
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.Word2VecModel

class MLJobActor(sc: SparkContext) extends Actor with ActorLogging {
  import MLJobActor._

  override def receive = {
    case RunKMeans =>
      KMeansOnTags.run(sc)
      sender() ! "Done"
    case ReloadModel =>
      synchronized {
        LikeMLApp.model = Word2VecModel.load(sc, "tag-w2v.model")
      }
      synchronized {
        LikeMLApp.cluster = KMeansModel.load(sc, "tag-cluster.model")
      }
      sender() ! "Done"
  }

}

object MLJobActor {
  def props(sc: SparkContext) = Props(classOf[MLJobActor], sc)
  case object RunKMeans
  case object ReloadModel
}