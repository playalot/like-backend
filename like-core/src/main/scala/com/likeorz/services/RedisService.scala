package com.likeorz.services

import javax.inject.Inject

import com.likeorz.utils.{ KeyUtils, RedisCacheClient }
import play.api.Configuration

class RedisService @Inject() (configuration: Configuration) {

  def renewCachesForUser(userId: Long, seconds: Int): Unit = {
    RedisCacheClient.expire(KeyUtils.user(userId), seconds)
    RedisCacheClient.expire(KeyUtils.timelineIds(userId), seconds)
    RedisCacheClient.expire(KeyUtils.timeline(userId), seconds)
  }

  // Update user last activity timestamp
  def updateActiveUser(userId: Long): Unit = {
    RedisCacheClient.zadd(KeyUtils.activeUsers, System.currentTimeMillis() / 1000, userId.toString)
  }

}
