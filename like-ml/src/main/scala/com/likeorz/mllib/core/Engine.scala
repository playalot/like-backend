package com.likeorz.mllib.core

case class Query(
  user: String,
  num: Int,
  categories: Option[Set[String]],
  whiteList: Option[Set[String]],
  blackList: Option[Set[String]]) extends Serializable

case class PredictedResult(
  itemScores: Array[ItemScore]) extends Serializable

case class ItemScore(
  item: String,
  score: Double) extends Serializable

class Engine {

}
