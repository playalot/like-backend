package utils

import play.api.Play
import scala.collection.JavaConversions._

object NlpUtils {

  val sensitiveWord = SensitiveWord.getInstance()

  val illegalWords = Play.current.configuration.getStringList("tag.illegal-words").get.toList

  def isContainSensitiveWord(word: String): Boolean = {
    if (illegalWords.exists(w => word.contains(w))) {
      true
    } else {
      sensitiveWord.isContainSensitiveWord(word)
    }
  }

}
