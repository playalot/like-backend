package com.likeorz.jobs

import com.likeorz.mllib.utils.RedisUtils
import com.typesafe.config.ConfigFactory
import org.apache.spark.{ SparkContext, SparkConf }

object HotTags {

  val conf = ConfigFactory.load()

  def run(days: Int) {
    println(s"$days Days Hot tags job start!")
    TrainingDataExport.exportTagsAndUsers(days)
    findHotTags(days)
  }

  def findHotTags(days: Int) {

    val PREFIX = conf.getString("train.prefix")
    // load model
    val sparkConf = new SparkConf().setMaster("local[1]").setAppName("like-hot-tags-daily")
      .set("spark.ui.port", "4041")
      .set("spark.driver.allowMultipleContexts", "true")

    val sc = new SparkContext(sparkConf)
    println("Build post category cache start...")
    try {
      sc.textFile(s"$PREFIX/marks_users_${days}d.csv")
        .map({ line =>
          val fields = line.split(",", 2)
          val userId = fields(0)
          val tag = fields(1)
          (tag, userId)
        })
        .groupByKey()
        .sortBy(x => x._2.size, ascending = false)
        .take(120)
        .map({
          case (tag, users) =>
            val group = users.groupBy(x => x).map(x => (x._1, x._2.size)).toSeq.sortBy(-_._2).take(100).map(_._1)
            (tag, group)
        })
        .foreach(x => RedisUtils.hset("hot_tags_users", x._1, x._2.mkString(",")))
    } catch {
      case e: Throwable => throw e
    } finally {
      sc.stop()
    }
  }

}
