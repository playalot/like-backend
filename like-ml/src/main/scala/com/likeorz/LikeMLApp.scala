package com.likeorz

import com.likeorz.jobs.{ RebuildCacheJob, UserCategories, KMeansOnTags, HotTags }
import com.likeorz.mllib.Test
import scopt.OptionParser

object LikeMLApp {

  case class MLParams(action: String = null)

  def main(args: Array[String]): Unit = {

    val defaultParams = MLParams("HotTags")

    val parser = new OptionParser[MLParams]("like-ml-daily") {
      head("like-ml", "1.0.0")
      opt[String]('a', "action").required().text(s"action to run")
        .action((x, c) => c.copy(action = x))
    }

    parser.parse(args, defaultParams).map { params =>
      params.action match {
        case "Test"               => Test.test()
        case "TrainCluster"       => KMeansOnTags.run(180)
        case "Cluster"            => UserCategories.run(30)
        case "HotTags"            => HotTags.run(30)
        case "Rebuild-UserCounts" => RebuildCacheJob.rebuildUserCountsCache()
        case "Rebuild-PostMarks"  => RebuildCacheJob.rebuildMarkCache()
        case "All" =>
          KMeansOnTags.run(180)
          UserCategories.run(30)
          HotTags.run(30)
        case "CleanCache" => Test.cleanLegacy()
      }

    }.getOrElse {
      sys.exit(1)
    }

  }

}
