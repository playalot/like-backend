package utils

import java.net.InetSocketAddress

import net.spy.memcached.{ AddrUtil, ConnectionFactoryBuilder, MemcachedClient }
import net.spy.memcached.ConnectionFactoryBuilder.Protocol
import net.spy.memcached.auth.{ AuthDescriptor, PlainCallbackHandler }

import play.api.Play

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.concurrent.duration._

import scalacache._
import memcached._

/**
 * Created by Guan Guan
 * Date: 5/23/15
 */
object MemcachedCacheClient {

  val host = Play.current.configuration.getString("memcached.host").get
  val port = Play.current.configuration.getInt("memcached.port").get

  val username = Play.current.configuration.getString("memcached.username").get
  val password = Play.current.configuration.getString("memcached.password").get

  val ad = new AuthDescriptor(Array("PLAIN"), new PlainCallbackHandler(username, password))

  val client = if (username == "") {
    new MemcachedClient(
      new InetSocketAddress(host, port))
  } else {
    new MemcachedClient(new ConnectionFactoryBuilder().setProtocol(Protocol.BINARY)
      .setAuthDescriptor(ad)
      .build(), AddrUtil.getAddresses(host + ":" + port))
  }

  implicit val scalaCache = ScalaCache(MemcachedCache(client))

  def save[T](key: String, value: T, expiration: Int): Boolean = client.set(key, expiration, value).get()

  def remove(key: String): Boolean = client.delete(key).get()

  def find[T: ClassTag](key: String): Option[T] = getSync(key)

  def saveAsync[T](key: String, value: T, expiration: Int): Future[Unit] = put(key)(value, ttl = Some(expiration.seconds))

  def removeAsync(key: String): Future[Unit] = scalaCache.cache.remove(key)

  def findAsync[T: ClassTag](key: String): Future[Option[T]] = get(key)

}
