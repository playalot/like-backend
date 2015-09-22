package com.likeorz.utils

import scala.concurrent.Future
import scala.util.{ Failure, Success }
import play.api.libs.concurrent.Execution.Implicits._

object FutureUtils {

  private val tZero = System.currentTimeMillis()

  def printlnC(s: Any): Unit = println(Console.GREEN + s + Console.RESET)
  def printlnE(s: Any): Unit = println(Console.RED + s + Console.RESET)

  def timedFuture[T](name: String)(f: Future[T]): Future[T] = {
    val start = System.currentTimeMillis()
    printlnC(s"--> started [$name] at t0 + ${start - tZero}")
    f.andThen {
      case Success(t) =>
        val end = System.currentTimeMillis()
        printlnC(s"\t<-- finished [$name] after ${end - start} millis")
      case Failure(ex) =>
        val end = System.currentTimeMillis()
        printlnE(s"\t<X> failed [$name], total time elapsed: ${end - start}\n$ex")
    }
  }

  def seqFutures[T, U](items: TraversableOnce[T])(func: T => Future[U]): Future[List[U]] = {
    items.foldLeft(Future.successful[List[U]](Nil)) {
      (f, item) =>
        f.flatMap {
          x => func(item).map(_ :: x)
        }
    } map (_.reverse)
  }

}
