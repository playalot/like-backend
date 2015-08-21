package com.likeorz.mllib.utils

import com.typesafe.config.ConfigFactory
import redis.clients.jedis.{ JedisPoolConfig, JedisPool, Jedis }
import scala.collection.JavaConversions._

object RedisUtils {

  val conf = ConfigFactory.load()
  val host = conf.getString("redis.host")
  val port = conf.getInt("redis.port")
  val auth = conf.getString("redis.auth")

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

  def zrevrangebyscore(key: String, max: Double = Double.MaxValue, min: Double = 0, offset: Int = 0, limit: Int = 15): Set[(String, Double)] = {
    withJedisClient[Set[(String, Double)]] { client =>
      client.zrevrangeByScoreWithScores(key, max, min, offset, limit).map(x => (x.getElement, x.getScore)).toSet
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
      val score = client.zscore(key, member)
      if (score == null) None
      else Some(score.toDouble)
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

  def sadd(key: String, members: Seq[String]) = {
    withJedisClient[Long] { client =>
      client.sadd(key, members: _*)
    }
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
    withJedisClient[List[String]] { client =>
      client.lrange(key, start, end).toList
    }
  }

  def zrevrangeWithScores(key: String, start: Long, end: Long): Map[String, Double] = {
    withJedisClient[Map[String, Double]] { client =>
      client.zrevrangeWithScores(key, start, end).map(p => (p.getElement -> p.getScore)).toMap
    }
  }

  private def withJedisClient[T](f: Jedis => T): T = {
    val jedis = jedisPool.getResource
    try {
      f(jedis)
    } finally {
      jedis.close()
    }
  }

}
