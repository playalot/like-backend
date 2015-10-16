package com.likeorz

import com.likeorz.jobs._
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
        case "Test"               => Test.run()
        case "TrainCluster"       => KMeansOnTags.run(180)
        case "Cluster"            => UserCategories.run(180)
        case "HotTags"            => HotTags.run(30)
        case "UserTags"           => UserTagsJob.populateUserTags()
        case "UserAddTag"         => UserTagsJob.addTagForAllUser(28)
        case "CategoryCache"      => UserCategories.buildCategoryCache(30)
        case "Rebuild-UserCounts" => RebuildCacheJob.rebuildUserCountsCache()
        case "Rebuild-UserLikes"  => RebuildCacheJob.rebuildUserLikes()
        case "Rebuild-PostMarks"  => RebuildCacheJob.rebuildMarkCache()
        case "Update-UserDBLikes" => RebuildCacheJob.updateDBUserLikes()
        case "RebuildTagUsage"    => RebuildIntervalCountJob.rebuildTagUsageCountCache()
        case "IncrementalUpdate"  => RebuildIntervalCountJob.incrementalUpdateTagUsageCount()
        case "SyncPost"           => SyncPostToMongoDB.SyncPostToMongoDB()
        case "All" =>
          KMeansOnTags.run(180)
          UserCategories.run(180)
          HotTags.run(30)
          RebuildCacheJob.updateDBUserLikes()
        case "CleanCache" => Test.cleanLegacy()
      }

    }.getOrElse {
      sys.exit(1)
    }

  }

}
