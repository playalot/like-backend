package utils

import play.api.Play
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.reflect.ClassTag
import scalacache._
import redis._

object RedisCacheClient {

  val host = Play.current.configuration.getString("redis.host").get
  val port = Play.current.configuration.getInt("redis.port").get

  implicit val scalaCache = ScalaCache(RedisCache(host, port))

  def save[T](key: String, value: T, expiration: Int): Future[T] = {
    scalacache.put[T](key)(value, ttl = Some(expiration.seconds)).map(_ => value)
  }

  def remove(key: String): Future[Unit] = {
    // Need to explicitly call remove from scalacache to avoid recursive
    scalacache.remove(key)
  }

  def find[T: ClassTag](key: String): Future[Option[T]] = scalacache.get[T](key)
}
