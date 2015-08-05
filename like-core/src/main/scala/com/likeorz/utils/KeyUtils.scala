package com.likeorz.utils

/**
 * Created by Guan Guan
 * Date: 6/30/15
 */
object KeyUtils {

  def session(token: String): String = "session_user:" + token

  def user(uid: Long): String = "user:" + uid

  def postSeen(uid: Long): String = "post_seen:" + uid

  def postMark(pid: Long): String = "post_mark:" + pid

  def postPromote: String = "post_promote"

  def userBlacklist: String = "user_blacklist"

  def userCategory: String = "user_category"

  def category(cid: Long): String = "category:" + cid

  def hotTags: String = "hot_tags"

  def hotUsers: String = "hot_users"

  def hotTagsWithUsers: String = "hot_tags_users"

  def pushLikes: String = "push_likes"
}

