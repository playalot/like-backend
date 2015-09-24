package utils

object QiniuUtil {

  val CDN = "http://cdn.likeorz.com"

  def resizeImage(filename: String, targetSize: Int, isParsedFilename: Boolean = false): String = {
    if (isParsedFilename) {
      val parts = filename.split("_")
      if (parts.size >= 6) {
        if (parts(3).toDouble < targetSize) {
          s"$CDN/$filename?imageView2/0/q/90"
        } else {
          s"$CDN/$filename?imageView2/0/w/$targetSize/q/90"
        }
      } else {
        s"$CDN/$filename?imageView2/0/w/$targetSize/h/$targetSize/q/90"
      }
    } else {
      s"$CDN/$filename?imageView2/1/w/$targetSize/h/$targetSize/q/90"
    }
  }

  def squareImage(filename: String, targetSize: Int, isParsedFilename: Boolean = true): String = {
    if (isParsedFilename) {
      val parts = filename.split("_")
      if (parts.size >= 6) {
        val min = scala.math.min(parts(3).toInt, parts(5).toInt)
        if (min < targetSize) {
          s"$CDN/$filename?imageView2/1/w/$min/q/90"
        } else {
          s"$CDN/$filename?imageView2/1/w/$targetSize/q/90"
        }
      } else {
        s"$CDN/$filename?imageView2/1/w/$targetSize/q/90"
      }
    } else {
      s"$CDN/$filename?imageView2/1/w/$targetSize/q/90"
    }
  }

}
