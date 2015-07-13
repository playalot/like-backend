package com.likeorz.mllib

import com.likeorz.mllib.core.{ LikeRecommendAlgorithm, LikeAlgorithmParams, Query, DataSource }
import com.likeorz.mllib.utils.FileUtils
import org.apache.spark.SparkContext
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{ read, write }

/**
 * Created by Guan Guan
 * Date: 7/6/15
 */
object CFRecommendation {

  def run(): Unit = {

    val sc = new SparkContext("local[4]", "like-cf-recommend")

    val ap = LikeAlgorithmParams("like-recommend", false, List(), List(), 10, 20, 0.01, Some(3L))

    val algorithm = new LikeRecommendAlgorithm(ap)

    val data = new DataSource().readTraining(sc)

    val model = algorithm.train(sc, data)

    println("Q1 ===============")

    val q1 = Query("715", 10, None, None, None)

    algorithm.predict(model, q1).itemScores.foreach(println)

    println("Q2 ===============")

    val q2 = Query("187", 10, None, None, None)

    algorithm.predict(model, q2).itemScores.foreach(println)

    implicit val formats = org.json4s.DefaultFormats

    println(write(model))

    //    FileUtils.writeString("model.dat", write(model))

    sc.stop()

  }

}
