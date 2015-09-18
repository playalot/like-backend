package com.likeorz.utils

object KeyUtils {

  def session(token: String): String = "session_user:" + token

  // hash
  def user(uid: Long): String = "user:" + uid

  // sorted set
  def timeline(userId: Long) = "timeline:" + userId

  // Set
  def timelineIds(userId: Long) = "timeline_ids:" + userId

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
  def newLikes: String = "push_likes"

  // set
  def bannedUsers: String = "banned_users"

  // white tag list
  def whiteListTags: String = "white_list_tags"

  // sorted set scheduled job to update tag usages from cache
  def tagUsage: String = "tag_usage"

  // sorted set scheduled job to update user likes from cache
  def userLikes: String = "user_likes"

  // sorted set mapping id -> name
  def tagNames: String = "tag_names"

  // Sorted set to store user notifications timestamp
  def userNotificationTimestamp: String = "notification_ts"

  // Track last user seen
  def activeUsers: String = "active_users"

}

