package com.likeorz.jobs

import java.io._

import com.likeorz.mllib.utils.{ FileUtils, MLUtils }
import com.typesafe.config.ConfigFactory
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.feature.Word2Vec
import org.apache.spark.{ SparkConf, SparkContext }

/**
 * Created by Guan Guan
 * Date: 7/3/15
 */
object KMeansOnTags {

  val conf = ConfigFactory.load()

  def run(days: Int) {
    println(s"$days Days cluster training job start!")
    TrainingDataExport.exportPostTags(days)
    runTrainModel(days)
  }

  def runTrainModel(days: Int): Unit = {
    // Load w2v and k-means model
    val sparkConf = new SparkConf().setMaster("local[1]").setAppName("like-ml-daily-train")
      .set("spark.ui.port", "4041")
      .set("spark.driver.allowMultipleContexts", "true")
    val sc = new SparkContext(sparkConf)

    println("Model training start!")

    try {
      val CLUSTER_NUM = conf.getInt("train.cluster-num")
      val MIN_WORD_COUNT = conf.getInt("train.min-word-count")
      val PREFIX = conf.getString("train.prefix")

      val posts_tags = sc.textFile(s"$PREFIX/post_tags_${days}d.csv").map { line =>
        line.split(",").toSeq.drop(3).flatMap(tag => MLUtils.cleanTag(tag)).flatMap(tag => MLUtils.tagDict(tag))
      }.filter(_.nonEmpty)

      val word2vec = new Word2Vec()
      word2vec.setMinCount(MIN_WORD_COUNT)
      word2vec.setNumIterations(30)
      word2vec.setVectorSize(MLUtils.VECTOR_SIZE)
      word2vec.setSeed(System.nanoTime())

      var preprocessStart = System.nanoTime()
      val model = word2vec.fit(posts_tags)
      var preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
      println(s"Model size: ${model.getVectors.size}")
      println(s"Preprocessing time: $preprocessElapsed sec")

      // Save w2v model
      FileUtils.deleteDir(s"$PREFIX/tag-w2v.model")
      model.save(sc, s"$PREFIX/tag-w2v.model")
      try {
        val fileOut = new FileOutputStream(s"$PREFIX/tag-w2v.model.dat")
        val objectOut = new ObjectOutputStream(fileOut)
        objectOut.writeObject(model)
        objectOut.close()
        System.out.println("The Object  was succesfully written to a file")
      } catch {
        case ex: Throwable => ex.printStackTrace()
      }

      println("美食:")
      model.findSynonyms("美食", 10).foreach(println)
      println("小米:")
      model.findSynonyms("小米", 10).foreach(println)
      println("萌:")
      model.findSynonyms("萌", 10).foreach(println)
      println("高达:")
      model.findSynonyms("高达", 10).foreach(println)
      println("壕:")
      model.findSynonyms("壕", 10).foreach(println)

      // Convert tags to vectors
      val tags_vectors = posts_tags.map(x => MLUtils.wordsToVector(x, model)).cache()

      val tags_pairs = posts_tags.map(x => (x, MLUtils.wordsToVector(x, model)))

      // KMeans cluster training
      val numClusters = CLUSTER_NUM
      val numIterations = 40

      preprocessStart = System.nanoTime()
      val clusters = KMeans.train(tags_vectors, numClusters, numIterations, 3)
      preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
      println(s"Preprocessing time: $preprocessElapsed sec")
      val wssse = clusters.computeCost(tags_vectors)
      println(s"K[$numClusters] - WSSSE for clusters: $wssse")

      // Save cluster model
      FileUtils.deleteDir(s"$PREFIX/tag-cluster.model")
      clusters.save(sc, s"$PREFIX/tag-cluster.model")
      try {
        val fileOut = new FileOutputStream(s"$PREFIX/tag-cluster.model.dat")
        val objectOut = new ObjectOutputStream(fileOut)
        objectOut.writeObject(clusters)
        objectOut.close()
        System.out.println("The Object  was succesfully written to a file")
      } catch {
        case ex: Throwable => ex.printStackTrace()
      }

      val article_membership = tags_pairs.map(x => (clusters.predict(x._2), x._1))
      val cluster_centers = sc.parallelize(clusters.clusterCenters.zipWithIndex.map { e => (e._2, e._1) })
      val cluster_topics = cluster_centers.mapValues(x => model.findSynonyms(x, 50).map(x => x._1))

      val sample_topic = cluster_topics.take(numClusters)
      var sample_members = article_membership.filter(x => x._1 == 6).take(10)

      for (i <- 0 until Math.min(5, numClusters)) {
        println("Topic Group #" + i)
        println(sample_topic(i)._2.mkString(","))
        println("-----------------------------")
        sample_members = article_membership.filter(x => x._1 == i).take(10)
        sample_members.foreach { x => println(x._2.mkString(" ")) }
        println("-----------------------------")
      }

      FileUtils.deleteDir(s"$PREFIX/tags_categorization")
      article_membership.map { x => x._1.toString + "," + x._2.mkString(" ") }.coalesce(1).saveAsTextFile(s"$PREFIX/tags_categorization")
      FileUtils.deleteDir(s"$PREFIX/tags_categories")
      cluster_topics.map { x => x._1 + "," + x._2.mkString(" ") }.coalesce(1).saveAsTextFile(s"$PREFIX/tags_categories")
    } catch {
      case e: Throwable => throw e
    } finally {
      sc.stop()
    }
    println("Model training stop!")
  }

}
