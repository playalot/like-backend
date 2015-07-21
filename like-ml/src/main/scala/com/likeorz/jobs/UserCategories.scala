package com.likeorz.jobs

import com.likeorz.mllib.utils.{ RedisUtils, MLUtils }
import com.typesafe.config.ConfigFactory
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.Word2VecModel
import org.apache.spark.{ SparkContext, SparkConf }
import org.joda.time.DateTime

object UserCategories {

  val conf = ConfigFactory.load()

  def run(days: Int) {
    println(s"Classify $days Days user category job start!")
    TrainingDataExport.exportPostTags(days)
    buildCategoryCache(days)
    buildUserCache(days)
  }
  // Populate posts to corresponding categories in cache
  def buildCategoryCache(days: Int): Unit = {
    val PREFIX = conf.getString("train.prefix")
    val CLUSTER_NUM = conf.getInt("train.cluster-num")
    // load model
    val sparkConf = new SparkConf().setMaster("local[1]").setAppName("like-ml-daily-post-cache")
      .set("spark.ui.port", "4041")
      .set("spark.driver.allowMultipleContexts", "true")
    val sc = new SparkContext(sparkConf)
    println("Build post category cache start...")
    try {
      val model = Word2VecModel.load(sc, s"$PREFIX/tag-w2v.model")
      val cluster = KMeansModel.load(sc, s"$PREFIX/tag-cluster.model")

      sc.textFile(s"$PREFIX/post_tags_${days}d.csv").map { line =>
        val fields = line.split(",", 4)
        (fields(0), fields(2).toLong, fields(3).split(",").toSeq.flatMap(tag => MLUtils.cleanTag(tag)))
      }.foreach { x =>
        val (id, ts, tags) = x
        val cat = cluster.predict(MLUtils.wordsToVector(tags, model))
        RedisUtils.zadd(s"cat_ts:$cat", ts, id)
        ()
      }
      // Clean expired category posts
      println("Remove expired items from cache...")
      val tsExpire = DateTime.now().minusDays(days).getMillis / 1000
      for (cat <- 0 to CLUSTER_NUM) {
        val deleteNum = RedisUtils.zremrangebyscore(s"cat_ts:$cat", 0, tsExpire)
        println(s"$deleteNum posts removed from category[$cat] cache")
      }

      println("Build post category cache done!")

    } catch {
      case e: Throwable => throw e
    } finally {
      sc.stop()
    }
  }

  // Build user cache keeping category ratio
  def buildUserCache(days: Int): Unit = {
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

      sc.textFile(s"$PREFIX/post_tags_${days}d.csv").map { line =>
        val fields = line.split(",", 4)
        fields(1) -> fields(3).split(",").toSeq.flatMap(tag => MLUtils.cleanTag(tag))
      }.groupByKey()
        .foreach { groups =>
          val counts = Array.fill[Int](ClusterNum)(0)
          groups._2.foreach { tags =>
            val cat = cluster.predict(MLUtils.wordsToVector(tags, model))
            counts(cat) += 1
          }
          val sum = counts.sum
          val userCategory = counts.map(x => scala.math.ceil(x * UserTotalItems.toDouble / sum)).mkString(",")
          RedisUtils.hset("user_category", groups._1, userCategory)
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
