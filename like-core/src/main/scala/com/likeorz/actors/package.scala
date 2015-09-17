package com.likeorz

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

package object actors {

  private val tZero = System.currentTimeMillis()

  def printlnC(s: Any): Unit = println(Console.GREEN + s + Console.RESET)
  def printlnE(s: Any): Unit = println(Console.RED + s + Console.RESET)

  def timedFuture[T](name: String)(f: Future[T])(implicit ec: ExecutionContext): Future[T] = {
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

}
