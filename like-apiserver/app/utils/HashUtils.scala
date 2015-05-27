package utils

import org.mindrot.jbcrypt.BCrypt

/**
 * Created by Guan Guan
 * Date: 5/23/15
 */
object HashUtils {

  def hashPassword(password: String): String = BCrypt.hashpw(password, BCrypt.gensalt)

  def validate(plain: String, hashed: String): Boolean = BCrypt.checkpw(plain, hashed)
}
