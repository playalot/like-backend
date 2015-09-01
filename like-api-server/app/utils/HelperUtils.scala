package utils

import scala.language.implicitConversions

object HelperUtils {

  private val rand = new scala.util.Random

  implicit def long2String(value: Long): String = value.toString

  implicit def int2String(value: Int): String = value.toString

  def parseTimestamp(timestamp: Option[String]): Seq[Option[Long]] = {
    if (timestamp.isDefined) {
      // parse timestamp string, e.g. 1435190198,1435185748,1435157044
      val tsArr = timestamp.get.split(",", -1).map {
        t => try { Some(t.toLong) } catch { case _: Throwable => None }
      }
      tsArr.toSeq
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

  def compareVersion(version1: String, version2: String): Boolean = {
    try {
      val parts1 = version1.split("\\.").map(_.toInt)
      val parts2 = version2.split("\\.").map(_.toInt)
      (1 to scala.math.min(parts1.length, parts2.length))
        .find(i => parts1(i) > parts2(i))
        .map(_ => true)
        .getOrElse(false)
    } catch {
      case e: Throwable => false
    }
  }

}
