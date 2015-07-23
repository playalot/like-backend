package com.likeorz.mllib

import com.likeorz.mllib.utils.{ RedisUtils, MLUtils }
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.Word2VecModel
import org.apache.spark.mllib.linalg.{ DenseVector, Vector }

object Test {

  def run(): Unit = {

    val sc = new SparkContext("local[4]", "like-recommend")

    val model = Word2VecModel.load(sc, "tag-w2v.model")

    println("=========")
    model.findSynonyms("eva", 100).map(_._1).foreach(println)
    println("=========")
    model.findSynonyms("古董", 100).map(_._1).foreach(println)
    println("=========")
    model.findSynonyms("摇滚", 100).map(_._1).foreach(println)
    println("=========")
    val cluster = KMeansModel.load(sc, "tag-cluster.model")

    val p1 = Seq("EVA")
    val v1 = new DenseVector(MLUtils.divArray(p1.map(m => MLUtils.wordToVector(m, model).toArray).reduceLeft(MLUtils.sumArray), p1.length)).asInstanceOf[Vector]
    val p2 = Seq("高达模型吧", "RG", "飞翼")
    val v2 = new DenseVector(MLUtils.divArray(p2.map(m => MLUtils.wordToVector(m, model).toArray).reduceLeft(MLUtils.sumArray), p2.length)).asInstanceOf[Vector]

    println("p1: " + cluster.predict(v1))
    println("p2: " + cluster.predict(v2))

    sc.stop()

  }

  def test(): Unit = {
  }

  def cleanLegacy() = {
    println("Clean cache")
    RedisUtils.keys("user_cat:*").foreach { key =>
      RedisUtils.del(key)
    }

    RedisUtils.keys("cat:*").foreach { key =>
      RedisUtils.del(key)
    }

  }

}
