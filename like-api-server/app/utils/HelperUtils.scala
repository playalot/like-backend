package utils

import scala.language.implicitConversions

/**
 * Created by Guan Guan
 * Date: 6/29/15
 */
object HelperUtils {

  private val rand = new scala.util.Random

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

  def insertAt[A](e: A, n: Int, ls: List[A]): List[A] = ls.splitAt(n) match {
    case (pre, post) => pre ::: e :: post
  }

  def random(i: Int, n: Int = 0): Int = {
    rand.nextInt(i) + n
  }

}
