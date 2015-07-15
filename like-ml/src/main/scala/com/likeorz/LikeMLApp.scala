package com.likeorz

import com.likeorz.mllib.{ TrainingDataExport, Test, KMeansOnTags }

object LikeMLApp {

  def main(args: Array[String]): Unit = {
    println("Job start!")
    TrainingDataExport.exportPostTags()
    KMeansOnTags.runTrainModel()
    KMeansOnTags.buildCategoryCache()
    KMeansOnTags.buildUserCache()
  }

}
