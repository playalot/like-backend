package com.likeorz.services

import com.likeorz.utils.{ KeyUtils, RedisCacheClient }

class RedisServiceImpl extends RedisService {

  override def renewCachesForUser(userId: Long, seconds: Int): Unit = {
    RedisCacheClient.expire(KeyUtils.user(userId), seconds)
    RedisCacheClient.expire(KeyUtils.timelineIds(userId), seconds)
    RedisCacheClient.expire(KeyUtils.timeline(userId), seconds)
  }

  // Update user last activity timestamp
  override def updateActiveUser(userId: Long): Unit = {
    RedisCacheClient.zadd(KeyUtils.activeUsers, System.currentTimeMillis() / 1000, userId.toString)
  }

}
