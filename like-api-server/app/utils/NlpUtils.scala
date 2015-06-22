package utils

import org.ansj.splitWord.analysis.BaseAnalysis
import scala.collection.JavaConversions._

/**
 * Created by Guan Guan
 * Date: 5/25/15
 */
object NlpUtils {

  def tokenize(str: String): Seq[String] = {
    BaseAnalysis.parse(str).map(t => t.getName).toSeq
  }
}
