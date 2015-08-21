package com.likeorz.mllib.utils

object KeyUtils {

  def session(token: String): String = "session_user:" + token

  // hash
  def user(uid: Long): String = "user:" + uid

  // Sorted set to store user notifications timestamp
  def userNotificationTimestamp: String = "notification_ts"

  // set
  def postSeen(uid: Long): String = "post_seen:" + uid

  // sorted set
  def postMark(pid: Long): String = "post_mark:" + pid

  // set
  def postPromote: String = "post_promote"

  // hash
  def userCategory: String = "user_category"

  // sorted set
  def category(cid: Long): String = "category:" + cid

  def hotTags: String = "hot_tags"

  def hotUsers: String = "hot_users"

  // hash
  def hotTagsWithUsers: String = "hot_tags_users"

  // sorted set
  def pushLikes: String = "push_likes"

  // set
  def bannedUsers: String = "banned_users"

  // white tag list
  def whiteListTags: String = "white_list_tags"

  def tagUsage: String = "tag_usage"

  def userLikes: String = "user_likes"

}

