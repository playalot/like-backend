package utils

import _root_.redis.clients.jedis.{ JedisPoolConfig, JedisPool, Jedis }
import play.api.Play
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._
import scala.concurrent.{ Future, blocking }
import scala.reflect.ClassTag
import scalacache._
import redis._
import scala.collection.JavaConversions._

object RedisCacheClient {

  val host = Play.current.configuration.getString("redis.host").get
  val port = Play.current.configuration.getInt("redis.port").get

  val username = Play.current.configuration.getString("redis.username").get
  val password = Play.current.configuration.getString("redis.password").get

  val jedisPool = if (username == "") {
    new JedisPool(new JedisPoolConfig(), host, port, 2000)
  } else {
    new JedisPool(new JedisPoolConfig(), host, port, 2000, username + ":" + password)
  }

  implicit val scalaCache = ScalaCache(RedisCache(jedisPool))

  def save[T](key: String, value: T, expiration: Int): Future[T] = {
    scalacache.put[T](key)(value, ttl = Some(expiration.seconds)).map(_ => value)
  }

  def remove(key: String): Future[Unit] = {
    // Need to explicitly call remove from scalacache to avoid recursive
    scalacache.remove(key)
  }

  def find[T: ClassTag](key: String): Future[Option[T]] = scalacache.get[T](key)

  def zRevRangeByScore(key: String): Set[(String, Double)] = {
    blocking {
      withJedisClient[Set[(String, Double)]] { client =>
        client.zrevrangeByScoreWithScores(key, Double.MaxValue, 0, 0, 15).map(x => (x.getElement, x.getScore)).toSet
      }
    }
  }

  private def withJedisClient[T](f: Jedis => T): T = {
    val jedis = jedisPool.getResource()
    try {
      f(jedis)
    } finally {
      jedis.close()
    }
  }

}
