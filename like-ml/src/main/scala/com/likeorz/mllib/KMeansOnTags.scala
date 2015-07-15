package com.likeorz.mllib

import com.likeorz.mllib.utils.{ RedisUtils, MLUtils, FileUtils }
import com.typesafe.config.ConfigFactory
import org.apache.spark.mllib.clustering.{ KMeansModel, KMeans }
import org.apache.spark.mllib.feature.{ Word2VecModel, Word2Vec }
import org.apache.spark.{ SparkConf, SparkContext }
import java.io._

import org.joda.time.DateTime

/**
 * Created by Guan Guan
 * Date: 7/3/15
 */
object KMeansOnTags {

  val conf = ConfigFactory.load()

  def runTrainModel(): Unit = {

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

      val posts_tags = sc.textFile(s"$PREFIX/post_tags.csv").map { line =>
        line.split(",").toSeq.drop(2).flatMap(tag => MLUtils.cleanTag(tag))
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
      //      FileUtils.deleteFile("tag-cluster.model.json")
      //      FileUtils.writeString("tag-cluster.model.json", jsonMapper.writeValueAsString(clusters))

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

  // Populate posts to corresponding categories in cache
  def buildCategoryCache(): Unit = {
    val PREFIX = conf.getString("train.prefix")
    // load model
    val sparkConf = new SparkConf().setMaster("local[1]").setAppName("like-ml-daily-post-cache")
      .set("spark.ui.port", "4041")
      .set("spark.driver.allowMultipleContexts", "true")
    val sc = new SparkContext(sparkConf)
    println("Build post category cache start...")
    try {
      val model = Word2VecModel.load(sc, s"$PREFIX/tag-w2v.model")
      val cluster = KMeansModel.load(sc, s"$PREFIX/tag-cluster.model")

      val tsNow = System.currentTimeMillis / 1000
      println(s"Timestamp: $tsNow")
      sc.textFile(s"$PREFIX/post_tags.csv").map { line =>
        val fields = line.split(",", 3)
        fields(0) -> fields(2).split(",").toSeq.flatMap(tag => MLUtils.cleanTag(tag))
      }.foreach { x =>
        val (id, tags) = x
        val cat = cluster.predict(MLUtils.wordsToVector(tags, model))
        RedisUtils.zincrby(s"cat:$cat", 1, id)
        RedisUtils.zadd(s"cat_ts:$cat", tsNow, id)
        ()
      }

      val CLUSTER_NUM = conf.getInt("train.cluster-num")
      println("Remove expired items from cache...")
      val tsOld = DateTime.now().minusDays(30).getMillis / 1000
      // remove posts older than 30 days
      println(s"Timestamp of 30 days ago: $tsOld")
      for (cat <- 0 until CLUSTER_NUM) {
        val removeItems = RedisUtils.zrangebyscore(s"cat_ts:$cat", 0, tsOld)
        RedisUtils.zrem(s"cat_ts:$cat", removeItems)
        RedisUtils.zrem(s"cat:$cat", removeItems)
        println(s"Category[$cat] removed ${removeItems.size} old items")
      }

      println("Build post category cache done!")

    } catch {
      case e: Throwable => throw e
    } finally {
      sc.stop()
    }
  }

  // Build user cache keeping category ratio
  def buildUserCache(): Unit = {
    val PREFIX = conf.getString("train.prefix")
    val sparkConf = new SparkConf().setMaster("local[1]").setAppName("like-ml-daily-user-category")
      .set("spark.ui.port", "4041")
      .set("spark.driver.allowMultipleContexts", "true")
    val sc = new SparkContext(sparkConf)
    println("Build user category cache start!")
    try {
      val UserTotalItems = conf.getInt("train.user-items-total")
      val ClusterNum = conf.getInt("train.cluster-num")

      // load model
      val model = Word2VecModel.load(sc, s"$PREFIX/tag-w2v.model")
      val cluster = KMeansModel.load(sc, s"$PREFIX/tag-cluster.model")

      sc.textFile(s"$PREFIX/post_tags.csv").map { line =>
        val fields = line.split(",", 3)
        fields(1) -> fields(2).split(",").toSeq.flatMap(tag => MLUtils.cleanTag(tag))
      }.groupByKey()
        .foreach { groups =>
          val counts = Array.fill[Int](ClusterNum)(0)
          groups._2.foreach { tags =>
            val cat = cluster.predict(MLUtils.wordsToVector(tags, model))
            counts(cat) += 1
          }
          val sum = counts.sum
          val userCategory = counts.map(x => scala.math.ceil(x * UserTotalItems.toDouble / sum).toString)
          RedisUtils.lpush("user_cat:" + groups._1, userCategory.reverse.toSeq: _*)
          RedisUtils.ltrim("user_cat:" + groups._1, 0, 100)
          ()
        }
      println("Build user category cache done!")
    } catch {
      case e: Throwable => throw e
    } finally {
      sc.stop()
    }
  }

}
