package com.likeorz.mllib

import com.likeorz.mllib.utils.{ RedisUtils, MLUtils }
import com.typesafe.config.ConfigFactory
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.Word2VecModel
import org.apache.spark.mllib.linalg.{ DenseVector, Vector }

object Test {

  val conf = ConfigFactory.load()

  def run(): Unit = {

    val PREFIX = conf.getString("train.prefix")

    val sc = new SparkContext("local[4]", "like-recommend")

    val model = Word2VecModel.load(sc, s"$PREFIX/tag-w2v.model")

    println("=========")
    model.findSynonyms("eva", 100).map(_._1).foreach(println)
    println("=========")
    model.findSynonyms("古董", 100).map(_._1).foreach(println)
    println("=========")
    model.findSynonyms("摇滚", 100).map(_._1).foreach(println)
    println("=========")
    val cluster = KMeansModel.load(sc, s"$PREFIX/tag-cluster.model")

    val p1 = Seq("福音战士", "香香")
    val v1 = MLUtils.wordsToVector(p1, model)
    println(v1)

    val p2 = Seq("高达模型吧", "RG", "飞翼")
    val v2 = new DenseVector(MLUtils.divArray(p2.map(m => MLUtils.wordToVector(m, model).toArray).reduceLeft(MLUtils.sumArray), p2.length)).asInstanceOf[Vector]
    val p3 = Seq()
    val v3 = new DenseVector(MLUtils.divArray(p2.map(m => MLUtils.wordToVector(m, model).toArray).reduceLeft(MLUtils.sumArray), p2.length)).asInstanceOf[Vector]
    val p4 = Seq("古董", "小可爱", "香水")
    val v4 = MLUtils.wordsToVector(p4, model)
    println(v4)

    println("p1: " + cluster.predict(v1))
    println("p2: " + cluster.predict(v2))
    println("p3: " + cluster.predict(v3))
    println("p4: " + cluster.predict(v4))

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

    RedisUtils.keys("post_seen:*").foreach { key =>
      RedisUtils.del(key)
    }

  }

}
