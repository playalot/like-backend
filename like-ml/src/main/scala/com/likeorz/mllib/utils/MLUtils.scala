package com.likeorz.mllib.utils

import org.apache.spark.mllib.feature.Word2VecModel
import org.apache.spark.mllib.linalg.{ DenseVector, Vectors, Vector }

object MLUtils {

  val VECTOR_SIZE = 200

  def cleanTag(tag: String): Seq[String] = {
    (if (tag.endsWith("吗") || tag.endsWith("么")) tag.substring(0, tag.length - 1)
    else tag).replace("...", "")
      .split(Array(' ', '!', ';', '?', '～', '。', '！', '；', '？', '-', '（', '）', '(', ')', '…'))
      .toSeq
      .filter(_.trim.length > 0)
      .map(_.toLowerCase)
  }

  def sumArray(m: Array[Double], n: Array[Double]): Array[Double] = {
    for (i <- m.indices) { m(i) += n(i) }
    m
  }

  def divArray(m: Array[Double], divisor: Double): Array[Double] = {
    for (i <- m.indices) { m(i) /= divisor }
    m
  }

  def wordToVector(w: String, m: Word2VecModel, vectorSize: Int = VECTOR_SIZE): Vector = {
    try {
      m.transform(w)
    } catch {
      case e: Exception => Vectors.zeros(vectorSize)
    }
  }

  def wordsToVector(tags: Seq[String], model: Word2VecModel): Vector = {
    // Sum vector and then divide vector to get average vector
    new DenseVector(MLUtils.divArray(tags.map(tag => MLUtils.wordToVector(tag, model).toArray).reduceLeft(MLUtils.sumArray), tags.length)).asInstanceOf[Vector]
  }

}
