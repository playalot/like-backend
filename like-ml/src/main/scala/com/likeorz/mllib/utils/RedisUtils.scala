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

  def zRevRangeByScore(key: String, max: Double = Double.MaxValue, min: Double = 0, offset: Int = 0, limit: Int = 15): Set[(String, Double)] = {
    withJedisClient[Set[(String, Double)]] { client =>
      client.zrevrangeByScoreWithScores(key, max, min, offset, limit).map(x => (x.getElement, x.getScore)).toSet
    }
  }

  def zScore(key: String, member: String): Option[Double] = {
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

  def zAdd(key: String, score: Double, member: String) = {
    withJedisClient[Long] { client =>
      client.zadd(key: String, score, member)
    }
  }

  def zIncrBy(key: String, score: Double, member: String) = {
    withJedisClient[Double] { client =>
      client.zincrby(key: String, score, member)
    }
  }

  def zRem(key: String, member: String) = {
    withJedisClient[Long] { client =>
      client.zrem(key, member)
    }
  }

  def sAdd(key: String, members: String*) = {
    withJedisClient[Long] { client =>
      client.sadd(key, members: _*)
    }
  }

  def sMembers(key: String) = {
    withJedisClient[Set[String]] { client =>
      client.smembers(key).toSet
    }
  }

  def srandmember(key: String, count: Int = 1) = {
    withJedisClient[List[String]] { client =>
      client.srandmember(key, count).toList
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

  def hget(key: String, field: String): String = {
    withJedisClient[String] { client =>
      client.hget(key, field)
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

  private def withJedisClient[T](f: Jedis => T): T = {
    val jedis = jedisPool.getResource
    try {
      f(jedis)
    } finally {
      jedisPool.returnResourceObject(jedis)
    }
  }

}
