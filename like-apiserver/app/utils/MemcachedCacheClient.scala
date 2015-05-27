package utils

import java.net.InetSocketAddress

import net.spy.memcached.{ AddrUtil, ConnectionFactoryBuilder, MemcachedClient }
import net.spy.memcached.ConnectionFactoryBuilder.Protocol
import net.spy.memcached.auth.{ AuthDescriptor, PlainCallbackHandler }
import net.spy.memcached.internal.{ GetCompletionListener, GetFuture, OperationFuture, OperationCompletionListener }

import play.api.Play

import scala.concurrent.{ Promise, Future }
import scala.reflect.ClassTag
import scala.collection.JavaConversions._

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
      new InetSocketAddress(host, port));
  } else {
    new MemcachedClient(new ConnectionFactoryBuilder().setProtocol(Protocol.BINARY)
      .setAuthDescriptor(ad)
      .build(), AddrUtil.getAddresses(host + ":" + port))

  }

  def save[T](key: String, value: T, expiration: Int): Boolean = client.set(key, expiration, value).get()

  def remove(key: String): Boolean = client.delete(key).get()

  def find[T: ClassTag](key: String): Option[T] = {
    val r = client.get(key)
    if (r == null)
      None
    else
      Some(r.asInstanceOf[T])
  }

  def saveAsync[T](key: String, value: T, expiration: Int): Future[Unit] = {
    asFutureOp(client.set(key, expiration, value), (result: java.lang.Boolean) => {})
  }

  def removeAsync(key: String): Future[Boolean] = {
    asFutureOp(client.delete(key), (result: java.lang.Boolean) => {
      result
    })
  }

  def findAsync[T: ClassTag](key: String): Future[Option[T]] = {
    asFutureGet(client.asyncGet(key), (r: Any) => {
      if (r == null)
        None
      else
        Some(r.asInstanceOf[T])
    })
  }

  private def asFutureOp[A, B](of: OperationFuture[A], f: A => B): Future[B] = {
    val p = Promise[B]
    of.addListener(new OperationCompletionListener {
      def onComplete(future: OperationFuture[_]) = {
        try {
          val result = future.get()
          p.success(f(result.asInstanceOf[A]))
        } catch {
          case t: Throwable =>
            p.failure(t)
        }
      }
    })
    p.future
  }

  private def asFutureGet[A, B](of: GetFuture[A], f: A => B): Future[B] = {
    val p = Promise[B]
    of.addListener(new GetCompletionListener {
      def onComplete(future: GetFuture[_]) = {
        try {
          val result = future.get()
          p.success(f(result.asInstanceOf[A]))
        } catch {
          case t: Throwable =>
            p.failure(t)
        }
      }
    })
    p.future
  }
}
