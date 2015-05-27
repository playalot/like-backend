package utils

import org.ansj.splitWord.analysis.NlpAnalysis
import scala.collection.JavaConversions._

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
object NlpUtils {

  def tokenize(str: String): Seq[String] = {
    NlpAnalysis.parse(str).map(t => t.getName).toSeq
  }
}
