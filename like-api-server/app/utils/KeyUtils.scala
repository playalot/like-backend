package utils

/**
 * Created by Guan Guan
 * Date: 6/30/15
 */
object KeyUtils {

  def session(token: String): String = "session_user:" + token

  def user(uid: Long): String = "user:" + uid

  def postMark(pid: Long): String = "post_mark:" + pid

  def postPromote: String = "post_promote"

  def userBlacklist: String = "user_blacklist"

}

