package com.likeorz.mllib.utils

import org.joda.time.DateTime

/**
 * Created by Guan Guan
 * Date: 7/6/15
 */
object FileUtils {

  def pathToFile(s: String) = new java.io.File(s)

  def unifyPath(path: String) = path.replaceAll("file://", "")

  def fileExists(path: String): Boolean = {
    new java.io.File(unifyPath(path)).exists()
  }

  def folderExists(path: String): Boolean = {
    val file = pathToFile(unifyPath(path))
    file.exists() && file.isDirectory
  }

  def readString(path: String): String = {
    scala.io.Source.fromFile(unifyPath(path)).mkString
  }

  def writeString(path: String, content: String): Unit = {
    val file = pathToFile(unifyPath(path))
    if (!file.exists()) {
      file.getParentFile.mkdirs()
    }
    val writer = new java.io.PrintWriter(file)
    writer.write(content)
    writer.close()
  }

  def deleteDir(path: String): Unit = {
    import scala.sys.process._
    assert(("rm -rf " + unifyPath(path)).! == 0)
  }

  def getStringIterator(path: String): Iterator[String] = {
    scala.io.Source.fromFile(unifyPath(path)).getLines()
  }

  def lastUpdate(path: String): DateTime = {
    new DateTime(new java.io.File(unifyPath(path)).lastModified())
  }

  def renameFile(path1: String, path2: String): Boolean = {
    try {
      pathToFile(unifyPath(path1)) renameTo pathToFile(unifyPath(path2))
      true
    } catch {
      case _: Throwable => false
    }
  }

  def copyFile(path1: String, path2: String): Unit = {
    import scala.sys.process._
    assert(s"cp ${unifyPath(path1)} ${unifyPath(path2)}".! == 0)
  }

  def getFile(path: String): java.io.File = {
    new java.io.File(unifyPath(path))
  }
}