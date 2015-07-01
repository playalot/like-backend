package utils

import scala.language.implicitConversions

/**
 * Created by Guan Guan
 * Date: 6/29/15
 */
object HelperUtils {

  implicit def long2String(value: Long): String = value.toString

  implicit def int2String(value: Int): String = value.toString

  def parseTimestamp(timestamp: Option[String]): Seq[Option[Long]] = {
    if (timestamp.isDefined) {
      // parse timestamp string, e.g. 1435190198,1435185748,1435157044
      val tsArr = timestamp.get.split(",").map {
        t => try { Some(t.toLong) } catch { case _: Throwable => None }
      }
      // Replace None with a minimum timestamp in the array, e.g 1435190198,,1435185748 => 1435190198,1435185748,1435185748
      if (tsArr.exists(_.isDefined)) {
        val min = tsArr.minBy(_.getOrElse(Long.MaxValue))
        tsArr.map(t => if (t.isDefined) t else min)
      } else {
        Seq(None, None, None)
      }
    } else {
      Seq(None, None, None)
    }
  }

}
