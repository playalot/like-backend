package com.likeorz.utils

import play.api.Play
import redis.clients.jedis.{ JedisPoolConfig, JedisPool, Jedis }
import scala.collection.JavaConversions._

object RedisCacheClient {

  val host = Play.current.configuration.getString("redis.host").get
  val port = Play.current.configuration.getInt("redis.port").get

  val auth = Play.current.configuration.getString("redis.auth").get

  val jedisPool = if (auth == "") {
    new JedisPool(new JedisPoolConfig(), host, port, 2000)
  } else {
    new JedisPool(new JedisPoolConfig(), host, port, 2000, auth)
  }

  def keys(pattern: String): Set[String] = {
    withJedisClient[Set[String]] { client =>
      client.keys(pattern).toSet
    }
  }

  def zrange(key: String, start: Long, end: Long): Set[String] = {
    withJedisClient { client =>
      client.zrange(key, start, end).toSet
    }
  }

  def zrevrangeByScoreWithScores(key: String, max: Double, min: Double, offset: Int, limit: Int): Set[(String, Double)] = {
    withJedisClient[Set[(String, Double)]] { client =>
      client.zrevrangeByScoreWithScores(key, max, min, offset, limit).map(x => (x.getElement, x.getScore)).toSet
    }
  }

  def zrevrangeByScoreWithScores(key: String, max: Double = Double.MaxValue, min: Double = 0): Set[(String, Double)] = {
    withJedisClient[Set[(String, Double)]] { client =>
      client.zrevrangeByScoreWithScores(key, max, min).map(x => (x.getElement, x.getScore)).toSet
    }
  }

  def zrevrangebyscore(key: String, min: Double, max: Double, offset: Int, count: Int) = {
    withJedisClient[Set[String]] { client =>
      client.zrevrangeByScore(key: String, max, min, offset, count).toSet
    }
  }

  def zrangebyscore(key: String, min: Double, max: Double) = {
    withJedisClient[Set[String]] { client =>
      client.zrangeByScore(key: String, min, max).toSet
    }
  }

  def zrangebyscore(key: String, min: Double, max: Double, offset: Int, count: Int) = {
    withJedisClient[Set[String]] { client =>
      client.zrangeByScore(key: String, min, max, offset, count).toSet
    }
  }

  def zremrangebyscore(key: String, min: Double, max: Double) = {
    withJedisClient[Long] { client =>
      client.zremrangeByScore(key: String, min, max)
    }
  }

  def zscore(key: String, member: String): Option[Double] = {
    withJedisClient[Option[Double]] { client =>
      Option(Double.unbox(client.zscore(key, member)))
    }
  }

  def del(keys: Seq[String]): Long = {
    withJedisClient[Long] { client =>
      client.del(keys: _*)
    }
  }

  def del(key: String): Long = {
    withJedisClient[Long] { client =>
      client.del(key)
    }
  }

  def zadd(key: String, score: Double, member: String) = {
    withJedisClient[Long] { client =>
      client.zadd(key: String, score, member)
    }
  }

  def zincrby(key: String, score: Double, member: String) = {
    withJedisClient[Double] { client =>
      client.zincrby(key: String, score, member)
    }
  }

  def zrem(key: String, member: String) = {
    withJedisClient[Long] { client =>
      client.zrem(key, member)
    }
  }

  def sadd(key: String, members: Seq[String]): Long = {
    withJedisClient { client => client.sadd(key, members: _*) }
  }

  def srem(key: String, members: Seq[String]): Long = {
    withJedisClient { client => client.srem(key, members: _*) }
  }

  def smembers(key: String) = {
    withJedisClient[Set[String]] { client =>
      client.smembers(key).toSet
    }
  }

  def srandmember(key: String, count: Int = 1) = {
    withJedisClient[Seq[String]] { client =>
      client.srandmember(key, count).toSeq
    }
  }

  def sismember(key: String, member: String) = {
    withJedisClient[Boolean] { client =>
      client.sismember(key, member)
    }
  }

  def hmset(key: String, fields: Map[String, String]) = {
    withJedisClient[String] { client =>
      client.hmset(key, fields)
    }
  }

  def hincrBy(key: String, field: String, value: Long) = {
    withJedisClient[Long] { client =>
      client.hincrBy(key, field, value)
    }
  }

  def hkeys(key: String): Set[String] = {
    withJedisClient[Set[String]] { client =>
      client.hkeys(key).toSet
    }
  }

  def hget(key: String, field: String): Option[String] = {
    withJedisClient[Option[String]] { client =>
      Option(client.hget(key, field))
    }
  }

  def hset(key: String, field: String, value: String): Long = {
    withJedisClient[Long] { client =>
      client.hset(key, field, value)
    }
  }

  def hexists(key: String, field: String): Boolean = {
    withJedisClient[Boolean] { client =>
      client.hexists(key, field)
    }
  }

  def hmget(key: String, fields: String*): List[String] = {
    withJedisClient[List[String]] { client =>
      client.hmget(key, fields.toSeq: _*).toList
    }
  }

  def hgetAll(key: String): Map[String, String] = {
    withJedisClient[Map[String, String]] { client =>
      client.hgetAll(key).toMap
    }
  }

  def lrange(key: String, start: Long, end: Long): List[String] = {
    withJedisClient { client => client.lrange(key, start, end).toList }
  }

  def zrevrange(key: String, start: Long, end: Long): List[String] = {
    withJedisClient { client => client.zrevrange(key, start, end).toList }
  }

  def zrevrangeWithScores(key: String, start: Long, end: Long): Seq[(String, Double)] = {
    withJedisClient { client => client.zrevrangeWithScores(key, start, end).map(p => p.getElement -> p.getScore).toSeq.sortBy(-_._2) }
  }

  def expire(key: String, seconds: Int): Long = {
    withJedisClient { client => client.expire(key, seconds) }
  }

  def rename(oldKey: String, newKey: String): Unit = {
    withJedisClient { client => client.rename(oldKey, newKey) }
  }

  def withJedisClient[T](f: Jedis => T): T = {
    val jedis = jedisPool.getResource
    try {
      f(jedis)
    } finally {
      jedis.close()
    }
  }

}
