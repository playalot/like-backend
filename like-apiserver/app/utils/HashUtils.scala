package utils

import org.mindrot.jbcrypt.BCrypt
import java.security.MessageDigest

/**
 * Created by Guan Guan
 * Date: 5/23/15
 */
object HashUtils {

  def hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt)

  def validate(plain: String, hashed: String): Boolean = BCrypt.checkpw(plain, hashed)

  def validateTimestampHash(value: String): Boolean = {
    val currentMinutes = System.currentTimeMillis() / 1000 / 60
    (0 to 4).exists( i => minuteToHash(currentMinutes - i) == value)
  }

  private def minuteToHash(minute: Long): String = {
    val map = Map('0'->"x", '1' -> "v", '2' -> ".", '3' -> "e",
      '4' -> "5", '5' -> "0", '6' -> ";", '7' -> "r",
      '8' -> "8", '9' -> "@")
    val mappedStr = minute.toString.map(c => map(c))
    val shuffledStr = mappedStr(4) + mappedStr(7) + mappedStr(0) + mappedStr(1) +
      mappedStr(5) + mappedStr(2) + mappedStr(6) + mappedStr(3)
    MessageDigest.getInstance("MD5").digest(shuffledStr.getBytes).map("%02X".format(_)).mkString
  }

}
