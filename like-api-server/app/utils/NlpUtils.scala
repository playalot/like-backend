package utils

import org.ansj.splitWord.analysis.BaseAnalysis
import play.api.Play
import scala.collection.JavaConversions._

object NlpUtils {

  val sensitiveWord = SensitiveWord.getInstance()

  val illegalWords = Play.current.configuration.getStringList("tag.illegal-words").get.toList

  def tokenize(str: String): Seq[String] = {
    BaseAnalysis.parse(str).map(t => t.getName).toSeq
  }

  def isContainSensitiveWord(word: String): Boolean = {
    if (illegalWords.exists(w => word.contains(w))) {
      true
    } else {
      sensitiveWord.isContainSensitiveWord(word)
    }
  }

}
