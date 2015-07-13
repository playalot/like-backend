package com.likeorz.mllib

import com.likeorz.mllib.core.AbstractParams
import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.{ EMLDAOptimizer, OnlineLDAOptimizer, DistributedLDAModel, LDA }
import org.apache.spark.mllib.linalg.{ Vector, Vectors }
import scala.collection.mutable

object LDAOnTags {

  case class Params(
    input: Seq[String] = Seq.empty,
    k: Int = 20,
    maxIterations: Int = 10,
    docConcentration: Double = -1,
    topicConcentration: Double = -1,
    vocabSize: Int = 10000,
    stopwordFile: String = "",
    algorithm: String = "em",
    checkpointDir: Option[String] = None,
    checkpointInterval: Int = 10) extends AbstractParams[Params]

  def run(params: Params = Params(Seq("post_tags.csv"), 5, 50)): Unit = {

    val defaultParams = Params()

    val sc = new SparkContext("local[4]", "lda")

    // Load documents, and prepare them for LDA.
    val preprocessStart = System.nanoTime()
    val (corpus, vocabArray, actualNumTokens) =
      preprocess(sc, params.input, params.vocabSize, params.stopwordFile)
    corpus.cache()
    val actualCorpusSize = corpus.count()
    val actualVocabSize = vocabArray.length
    val preprocessElapsed = (System.nanoTime() - preprocessStart) / 1e9

    println()
    println(s"Corpus summary:")
    println(s"\t Training set size: $actualCorpusSize documents")
    println(s"\t Vocabulary size: $actualVocabSize terms")
    println(s"\t Training set size: $actualNumTokens tokens")
    println(s"\t Preprocessing time: $preprocessElapsed sec")
    println()

    // Run LDA.
    val lda = new LDA()

    val optimizer = params.algorithm.toLowerCase match {
      case "em"     => new EMLDAOptimizer
      // add (1.0 / actualCorpusSize) to MiniBatchFraction be more robust on tiny datasets.
      case "online" => new OnlineLDAOptimizer().setMiniBatchFraction(0.05 + 1.0 / actualCorpusSize)
      case _ => throw new IllegalArgumentException(
        s"Only em, online are supported but got ${params.algorithm}.")
    }

    lda.setOptimizer(optimizer)
      .setK(params.k)
      .setMaxIterations(params.maxIterations)
    //      .setDocConcentration(params.docConcentration)
    //      .setTopicConcentration(params.topicConcentration)
    //      .setCheckpointInterval(params.checkpointInterval)
    if (params.checkpointDir.nonEmpty) {
      sc.setCheckpointDir(params.checkpointDir.get)
    }
    val startTime = System.nanoTime()
    val ldaModel = lda.run(corpus)
    val elapsed = (System.nanoTime() - startTime) / 1e9

    println(s"Finished training LDA model.  Summary:")
    println(s"\t Training time: $elapsed sec")

    if (ldaModel.isInstanceOf[DistributedLDAModel]) {
      val distLDAModel = ldaModel.asInstanceOf[DistributedLDAModel]
      val avgLogLikelihood = distLDAModel.logLikelihood / actualCorpusSize.toDouble
      println(s"\t Training data average log likelihood: $avgLogLikelihood")
      println()
    }

    // Print the topics, showing the top-weighted terms for each topic.
    val topicIndices = ldaModel.describeTopics(maxTermsPerTopic = 30)
    val topics = topicIndices.map {
      case (terms, termWeights) =>
        terms.zip(termWeights).map { case (term, weight) => (vocabArray(term.toInt), weight) }
    }
    println(s"${params.k} topics:")
    topics.zipWithIndex.foreach {
      case (topic, i) =>
        println(s"TOPIC $i")
        topic.foreach {
          case (term, weight) =>
            println(s"$term\t$weight")
        }
        println()
    }

    println("Learned topics (as distributions over vocab of " + ldaModel.vocabSize + " words):")
    val topics2 = ldaModel.topicsMatrix
    for (topic <- Range(0, params.k)) {
      print("Topic " + topic + ":")
      for (word <- Range(0, ldaModel.vocabSize)) { print(" " + topics2(word, topic)); }
      println()
    }

    sc.stop()

  }

  /**
   * Load documents, tokenize them, create vocabulary, and prepare documents as term count vectors.
   * @return (corpus, vocabulary as array, total token count in corpus)
   */
  private def preprocess(
    sc: SparkContext,
    paths: Seq[String],
    vocabSize: Int,
    stopwordFile: String): (RDD[(Long, Vector)], Array[String], Long) = {

    // Get dataset of document texts
    // One document per line in each text file. If the input consists of many small files,
    // this can result in a large number of small partitions, which can degrade performance.
    // In this case, consider using coalesce() to create fewer, larger partitions.
    val textRDD: RDD[String] = sc.textFile(paths.mkString(","))

    // Split text into words
    // val tokenizer = new SimpleTokenizer(sc, stopwordFile)
    val tokenized: RDD[(Long, IndexedSeq[String])] = textRDD.map { line =>
      val words = line.split(",")
      val tokens = words.drop(1)
        .flatMap(_.split(Array(' ', '!', ';', '?', '。', '！', '；', '？', '-')))
        .filter(_.trim.length > 0)
        .toIndexedSeq
      words(0).toLong -> tokens
    }
    tokenized.cache()

    // Counts words: RDD[(word, wordCount)]
    val wordCounts: RDD[(String, Long)] = tokenized
      .flatMap { case (_, tokens) => tokens.map(_ -> 1L) }
      .reduceByKey(_ + _)
    wordCounts.cache()
    val fullVocabSize = wordCounts.count()
    // Select vocab
    //  (vocab: Map[word -> id], total tokens after selecting vocab)
    val (vocab: Map[String, Int], selectedTokenCount: Long) = {
      val tmpSortedWC: Array[(String, Long)] = if (vocabSize == -1 || fullVocabSize <= vocabSize) {
        // Use all terms
        wordCounts.collect().sortBy(-_._2)
      } else {
        // Sort terms to select vocab
        wordCounts.sortBy(_._2, ascending = false).take(vocabSize)
      }
      (tmpSortedWC.map(_._1).zipWithIndex.toMap, tmpSortedWC.map(_._2).sum)
    }

    val documents = tokenized.map {
      case (id, tokens) =>
        // Filter tokens by vocabulary, and create word count vector representation of document.
        val wc = new mutable.HashMap[Int, Int]()
        tokens.foreach { term =>
          if (vocab.contains(term)) {
            val termIndex = vocab(term)
            wc(termIndex) = wc.getOrElse(termIndex, 0) + 1
          }
        }
        val indices = wc.keys.toArray.sorted
        val values = indices.map(i => wc(i).toDouble)

        val sb = Vectors.sparse(vocab.size, indices, values)
        (id, sb)
    }

    val vocabArray = new Array[String](vocab.size)
    vocab.foreach { case (term, i) => vocabArray(i) = term }

    (documents, vocabArray, selectedTokenCount)
  }

}
