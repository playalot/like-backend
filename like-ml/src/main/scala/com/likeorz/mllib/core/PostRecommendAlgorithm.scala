package com.likeorz.mllib.core

import com.likeorz.mllib.utils.BiMap
import grizzled.slf4j.Logger
import org.apache.spark.SparkContext
import org.apache.spark.mllib.recommendation.{ ALS, Rating => MLlibRating }
import org.apache.spark.rdd.RDD

import scala.collection.mutable.PriorityQueue

/**
 * Created by Guan Guan
 * Date: 7/6/15
 */
case class LikeAlgorithmParams(
  appName: String,
  unseenOnly: Boolean,
  seenEvents: List[String],
  similarEvents: List[String],
  rank: Int,
  numIterations: Int,
  lambda: Double,
  seed: Option[Long])

case class ProductModel(
  item: Post,
  features: Option[Array[Double]], // features by ALS
  count: Int // popular count for default score
  )

class LikeModel(
    val rank: Int,
    val userFeatures: Map[Int, Array[Double]],
    val productModels: Map[Int, ProductModel],
    val userStringIntMap: BiMap[String, Int],
    val postStringIntMap: BiMap[String, Int]) extends Serializable {

  @transient lazy val itemIntStringMap = postStringIntMap.inverse

  override def toString = {
    s" rank: ${rank}" +
      s" userFeatures: [${userFeatures.size}]" +
      s"(${userFeatures.take(2).toList}...)" +
      s" productModels: [${productModels.size}]" +
      s"(${productModels.take(2).toList}...)" +
      s" userStringIntMap: [${userStringIntMap.size}]" +
      s"(${userStringIntMap.take(2).toString}...)]" +
      s" postStringIntMap: [${postStringIntMap.size}]" +
      s"(${postStringIntMap.take(2).toString}...)]"
  }
}

class LikeRecommendAlgorithm(val ap: LikeAlgorithmParams) extends Serializable {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, data: TrainingData): LikeModel = {
    require(!data.likeEvents.take(1).isEmpty,
      s"likeEvents in PreparedData cannot be empty." +
        " Please check if DataSource generates TrainingData" +
        " and Preprator generates PreparedData correctly.")
    require(!data.users.take(1).isEmpty,
      s"users in PreparedData cannot be empty." +
        " Please check if DataSource generates TrainingData" +
        " and Preprator generates PreparedData correctly.")
    require(!data.posts.take(1).isEmpty,
      s"posts in PreparedData cannot be empty." +
        " Please check if DataSource generates TrainingData" +
        " and Preprator generates PreparedData correctly.")

    // create User and item's String ID to integer index BiMap
    val userStringIntMap = BiMap.stringInt(data.users.keys)
    val postStringIntMap = BiMap.stringInt(data.posts.keys)

    val mllibRatings: RDD[MLlibRating] = genMLlibRating(
      userStringIntMap = userStringIntMap,
      postStringIntMap = postStringIntMap,
      data = data
    )

    // MLLib ALS cannot handle empty training data.
    require(!mllibRatings.take(1).isEmpty,
      s"mllibRatings cannot be empty." +
        " Please check if your events contain valid user and item ID.")

    // seed for MLlib ALS
    val seed = ap.seed.getOrElse(System.nanoTime)

    // use ALS to train feature vectors
    val m = ALS.trainImplicit(
      ratings = mllibRatings,
      rank = ap.rank,
      iterations = ap.numIterations,
      lambda = ap.lambda,
      blocks = -1,
      alpha = 1.0,
      seed = seed)

    val userFeatures = m.userFeatures.collectAsMap.toMap

    // convert ID to Int index
    val posts = data.posts.map {
      case (id, item) =>
        (postStringIntMap(id), item)
    }

    // join item with the trained productFeatures
    val productFeatures: Map[Int, (Post, Option[Array[Double]])] =
      posts.leftOuterJoin(m.productFeatures).collectAsMap.toMap

    val popularCount = trainDefault(
      userStringIntMap = userStringIntMap,
      postStringIntMap = postStringIntMap,
      data = data
    )

    val productModels: Map[Int, ProductModel] = productFeatures
      .map {
        case (index, (item, features)) =>
          val pm = ProductModel(
            item = item,
            features = features,
            // NOTE: use getOrElse because popularCount may not contain all items.
            count = popularCount.getOrElse(index, 0)
          )
          (index, pm)
      }

    new LikeModel(
      rank = m.rank,
      userFeatures = userFeatures,
      productModels = productModels,
      userStringIntMap = userStringIntMap,
      postStringIntMap = postStringIntMap
    )
  }

  /**
   * Generate MLlibRating from PreparedData.
   * You may customize this function if use different events or different aggregation method
   */
  def genMLlibRating(
    userStringIntMap: BiMap[String, Int],
    postStringIntMap: BiMap[String, Int],
    data: TrainingData): RDD[MLlibRating] = {

    val mllibRatings = data.likeEvents
      .map { r =>
        // Convert user and item String IDs to Int index for MLlib
        val uindex = userStringIntMap.getOrElse(r.user, -1)
        val iindex = postStringIntMap.getOrElse(r.item, -1)

        if (uindex == -1)
          logger.info(s"Couldn't convert nonexistent user ID ${r.user}"
            + " to Int index.")

        if (iindex == -1)
          logger.info(s"Couldn't convert nonexistent item ID ${r.item}"
            + " to Int index.")

        ((uindex, iindex), 1)
      }
      .filter {
        case ((u, i), v) =>
          // keep events with valid user and item index
          (u != -1) && (i != -1)
      }
      .reduceByKey(_ + _) // aggregate all view events of same user-item pair
      .map {
        case ((u, i), v) =>
          // MLlibRating requires integer index for user and item
          MLlibRating(u, i, v)
      }
      .cache()

    mllibRatings
  }

  /**
   * Train default model.
   * You may customize this function if use different events or
   * need different ways to count "popular" score or return default score for item.
   */
  def trainDefault(
    userStringIntMap: BiMap[String, Int],
    postStringIntMap: BiMap[String, Int],
    data: TrainingData): Map[Int, Int] = {
    // count number of buys
    // (item index, count)
    val likeCountsRDD: RDD[(Int, Int)] = data.likeEvents
      .map { r =>
        // Convert user and item String IDs to Int index
        val uindex = userStringIntMap.getOrElse(r.user, -1)
        val iindex = postStringIntMap.getOrElse(r.item, -1)

        if (uindex == -1)
          logger.info(s"Couldn't convert nonexistent user ID ${r.user}"
            + " to Int index.")

        if (iindex == -1)
          logger.info(s"Couldn't convert nonexistent item ID ${r.item}"
            + " to Int index.")

        (uindex, iindex, 1)
      }
      .filter {
        case (u, i, v) =>
          // keep events with valid user and item index
          (u != -1) && (i != -1)
      }
      .map { case (u, i, v) => (i, 1) } // key is item
      .reduceByKey { case (a, b) => a + b } // count number of items occurrence

    likeCountsRDD.collectAsMap.toMap
  }

  def predict(model: LikeModel, query: Query): PredictedResult = {

    val userFeatures = model.userFeatures
    val productModels = model.productModels

    // convert whiteList's string ID to integer index
    val whiteList: Option[Set[Int]] = query.whiteList.map(set =>
      set.flatMap(model.postStringIntMap.get(_))
    )

    val finalBlackList: Set[Int] = Set()

    val userFeature: Option[Array[Double]] =
      model.userStringIntMap.get(query.user).flatMap { userIndex =>
        userFeatures.get(userIndex)
      }

    val topScores: Array[(Int, Double)] = if (userFeature.isDefined) {
      // the user has feature vector
      predictKnownUser(
        userFeature = userFeature.get,
        productModels = productModels,
        query = query,
        whiteList = whiteList,
        blackList = finalBlackList
      )
    } else {
      // the user doesn't have feature vector.
      // For example, new user is created after model is trained.
      logger.info(s"No userFeature found for user ${query.user}.")

      // check if the user has recent events on some items
      val recentItems: Set[String] = Set()
      //getRecentItems(query)
      val recentList: Set[Int] = recentItems.flatMap(x =>
        model.postStringIntMap.get(x))

      val recentFeatures: Vector[Array[Double]] = recentList.toVector
        // productModels may not contain the requested item
        .flatMap { i =>
          productModels.get(i).flatMap { pm => pm.features }
        }

      if (recentFeatures.isEmpty) {
        logger.info(s"No features vector for recent items $recentItems.")
        predictDefault(
          productModels = productModels,
          query = query,
          whiteList = whiteList,
          blackList = finalBlackList
        )
      } else {
        predictSimilar(
          recentFeatures = recentFeatures,
          productModels = productModels,
          query = query,
          whiteList = whiteList,
          blackList = finalBlackList
        )
      }
    }

    val itemScores = topScores.map {
      case (i, s) =>
        new ItemScore(
          // convert item int index back to string ID
          item = model.itemIntStringMap(i),
          score = s
        )
    }

    new PredictedResult(itemScores)
  }

  /** Prediction for user with known feature vector */
  def predictKnownUser(
    userFeature: Array[Double],
    productModels: Map[Int, ProductModel],
    query: Query,
    whiteList: Option[Set[Int]],
    blackList: Set[Int]): Array[(Int, Double)] = {
    val indexScores: Map[Int, Double] = productModels.par // convert to parallel collection
      .filter {
        case (i, pm) =>
          pm.features.isDefined &&
            isCandidateItem(
              i = i,
              item = pm.item,
              categories = query.categories,
              whiteList = whiteList,
              blackList = blackList
            )
      }
      .map {
        case (i, pm) =>
          // NOTE: features must be defined, so can call .get
          val s = dotProduct(userFeature, pm.features.get)
          // may customize here to further adjust score
          (i, s)
      }
      .filter(_._2 > 0) // only keep items with score > 0
      .seq // convert back to sequential collection

    val ord = Ordering.by[(Int, Double), Double](_._2).reverse
    val topScores = getTopN(indexScores, query.num)(ord).toArray

    topScores
  }

  /** Default prediction when know nothing about the user */
  def predictDefault(
    productModels: Map[Int, ProductModel],
    query: Query,
    whiteList: Option[Set[Int]],
    blackList: Set[Int]): Array[(Int, Double)] = {
    val indexScores: Map[Int, Double] = productModels.par // convert back to sequential collection
      .filter {
        case (i, pm) =>
          isCandidateItem(
            i = i,
            item = pm.item,
            categories = query.categories,
            whiteList = whiteList,
            blackList = blackList
          )
      }
      .map {
        case (i, pm) =>
          // may customize here to further adjust score
          (i, pm.count.toDouble)
      }
      .seq

    val ord = Ordering.by[(Int, Double), Double](_._2).reverse
    val topScores = getTopN(indexScores, query.num)(ord).toArray

    topScores
  }

  /** Return top similar items based on items user recently has action on */
  def predictSimilar(
    recentFeatures: Vector[Array[Double]],
    productModels: Map[Int, ProductModel],
    query: Query,
    whiteList: Option[Set[Int]],
    blackList: Set[Int]): Array[(Int, Double)] = {
    val indexScores: Map[Int, Double] = productModels.par // convert to parallel collection
      .filter {
        case (i, pm) =>
          pm.features.isDefined &&
            isCandidateItem(
              i = i,
              item = pm.item,
              categories = query.categories,
              whiteList = whiteList,
              blackList = blackList
            )
      }
      .map {
        case (i, pm) =>
          val s = recentFeatures.map { rf =>
            // pm.features must be defined because of filter logic above
            cosine(rf, pm.features.get)
          }.sum
          // may customize here to further adjust score
          (i, s)
      }
      .filter(_._2 > 0) // keep items with score > 0
      .seq // convert back to sequential collection

    val ord = Ordering.by[(Int, Double), Double](_._2).reverse
    val topScores = getTopN(indexScores, query.num)(ord).toArray

    topScores
  }

  private def getTopN[T](s: Iterable[T], n: Int)(implicit ord: Ordering[T]): Seq[T] = {

    val q = PriorityQueue()

    for (x <- s) {
      if (q.size < n)
        q.enqueue(x)
      else {
        // q is full
        if (ord.compare(x, q.head) < 0) {
          q.dequeue()
          q.enqueue(x)
        }
      }
    }

    q.dequeueAll.toSeq.reverse
  }

  private def dotProduct(v1: Array[Double], v2: Array[Double]): Double = {
    val size = v1.length
    var i = 0
    var d: Double = 0
    while (i < size) {
      d += v1(i) * v2(i)
      i += 1
    }
    d
  }

  private def cosine(v1: Array[Double], v2: Array[Double]): Double = {
    val size = v1.length
    var i = 0
    var n1: Double = 0
    var n2: Double = 0
    var d: Double = 0
    while (i < size) {
      n1 += v1(i) * v1(i)
      n2 += v2(i) * v2(i)
      d += v1(i) * v2(i)
      i += 1
    }
    val n1n2 = math.sqrt(n1) * math.sqrt(n2)
    if (n1n2 == 0) 0 else d / n1n2
  }

  private def isCandidateItem(
    i: Int,
    item: Post,
    categories: Option[Set[String]],
    whiteList: Option[Set[Int]],
    blackList: Set[Int]): Boolean = {
    // can add other custom filtering here
    whiteList.map(_.contains(i)).getOrElse(true) &&
      !blackList.contains(i) &&
      // filter categories
      categories.map { cat =>
        item.tags.exists { itemCat =>
          // keep this item if has ovelap categories with the query
          itemCat.toSet.intersect(cat).nonEmpty
        }
      }.getOrElse(true)

  }

}
