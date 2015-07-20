package utils

import com.qiniu.storage.UploadManager
import com.qiniu.util.Auth
import play.api.Play

object QiniuUtil {

  val CDN = Play.current.configuration.getString("qiniu.cdn").get

  val AvatarSize = Map(
    "large" -> Play.current.configuration.getInt("image.avatar.large").get,
    "medium" -> Play.current.configuration.getInt("image.avatar.medium").get,
    "small" -> Play.current.configuration.getInt("image.avatar.small").get,
    "origin" -> Play.current.configuration.getInt("image.avatar.origin").get
  )

  val PhotoSize = Map(
    "large" -> Play.current.configuration.getInt("image.photo.large").get,
    "medium" -> Play.current.configuration.getInt("image.photo.medium").get,
    "small" -> Play.current.configuration.getInt("image.photo.small").get,
    "origin" -> Play.current.configuration.getInt("image.photo.origin").get
  )

  val QiniuAccessKey = Play.current.configuration.getString("qiniu.accesskey").get
  val QiniuSecretKey = Play.current.configuration.getString("qiniu.secretkey").get

  val DefaultBucket = Play.current.configuration.getString("qiniu.bucket").get

  val QiniuUploadManager = new UploadManager()
  val QiniuAuth = Auth.create(QiniuAccessKey, QiniuSecretKey)

  def getUploadToken(bucket: String = DefaultBucket): String = {
    QiniuAuth.uploadToken(bucket)
  }

  def resizeImage(filename: String, targetSize: Int, isParsedFilename: Boolean = false): String = {
    if (isParsedFilename) {
      val parts = filename.split("_")
      if (parts.size >= 6) {
        if (parts(3).toDouble < targetSize) {
          s"$filename?imageView2/0/q/90"
        } else {
          s"$filename?imageView2/0/w/$targetSize/q/90"
        }
      } else {
        s"$filename?imageView2/0/w/$targetSize/h/$targetSize/q/90"
      }
    } else {
      s"$filename?imageView2/0/w/$targetSize/h/$targetSize/q/90"
    }
  }

  def getRaw(filename: String): String = {
    s"$CDN/$filename?imageView2/0/q/85"
  }

  def getScale(filename: String, targetSize: Int): String = {
    s"$CDN/$filename?imageView2/0/w/$targetSize/h/$targetSize/q/85"
  }

  def getAvatar(filename: String, size: String): String = {
    s"$CDN/${resizeImage(filename, AvatarSize.getOrElse(size, AvatarSize("small")))}/q/85"
  }

  def getPhoto(filename: String, size: String): String = {
    s"$CDN/${resizeImage(filename, PhotoSize.getOrElse(size, PhotoSize("small")))}/q/85"
  }

  def getSizedImage(filename: String, screenSize: Int): String = {
    s"$CDN/${resizeImage(filename, screenSize, true)}"
  }

}
