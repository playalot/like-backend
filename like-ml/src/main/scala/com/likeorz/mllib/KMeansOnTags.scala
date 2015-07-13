package com.likeorz.mllib

import com.likeorz.mllib.utils.{ MLUtils, FileUtils }
import org.apache.spark.mllib.clustering.KMeans
import org.apache.spark.mllib.feature.Word2Vec
import org.apache.spark.SparkContext

/**
 * Created by Guan Guan
 * Date: 7/3/15
 */
object KMeansOnTags {

  def run(sc: SparkContext): Unit = {

    try {
      val posts_tags = sc.textFile("post_tags.csv").map { line =>
        line.split(",").toSeq.drop(2).flatMap(tag => MLUtils.cleanTag(tag))
      }.filter(_.nonEmpty)

      val word2vec = new Word2Vec()
      word2vec.setMinCount(2)
      word2vec.setNumIterations(20)
      word2vec.setVectorSize(MLUtils.VECTOR_SIZE)
      word2vec.setSeed(System.nanoTime())

      val preprocessStart = System.nanoTime()
      val model = word2vec.fit(posts_tags)
      val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9
      println("\tModel size: " + model.getVectors.size)
      println(s"\t Preprocessing time: $preprocessElapsed sec")

      // Save w2v model
      FileUtils.deleteDir("tag-w2v.model")
      model.save(sc, "tag-w2v.model")

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
      val numClusters = 20
      val numIterations = 40
      val clusters = KMeans.train(tags_vectors, numClusters, numIterations, 3)

      // Save cluster model
      FileUtils.deleteDir("tag-cluster.model")
      clusters.save(sc, "tag-cluster.model")

      val wssse = clusters.computeCost(tags_vectors)
      println(s"K[$numClusters] - WSSSE for clusters: $wssse")

      val article_membership = tags_pairs.map(x => (clusters.predict(x._2), x._1))
      val cluster_centers = sc.parallelize(clusters.clusterCenters.zipWithIndex.map { e => (e._2, e._1) })
      val cluster_topics = cluster_centers.mapValues(x => model.findSynonyms(x, 20).map(x => x._1))

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

      FileUtils.deleteDir("tags_categorization")
      article_membership.map { x => x._1.toString + "," + x._2.mkString(" ") }.coalesce(1).saveAsTextFile("tags_categorization")
      FileUtils.deleteDir("tags_categories")
      cluster_topics.map { x => x._1 + "," + x._2.mkString(" ") }.coalesce(1).saveAsTextFile("tags_categories")

    } catch {
      case e: Throwable => e.printStackTrace()
    }
  }

}
