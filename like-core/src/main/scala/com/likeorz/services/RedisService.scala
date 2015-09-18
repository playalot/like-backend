package com.likeorz.services

trait RedisService {

  def renewCachesForUser(userId: Long, seconds: Int): Unit

  def updateActiveUser(userId: Long): Unit

}
