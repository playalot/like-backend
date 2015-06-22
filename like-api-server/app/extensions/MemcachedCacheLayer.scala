package extensions

import com.mohiva.play.silhouette.api.util.CacheLayer

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

/**
 * Created by Guan Guan
 * Date: 5/24/15
 */
class MemcachedCacheLayer extends CacheLayer {

  override def find[T: ClassTag](key: String): Future[Option[T]] = ???

  override def remove(key: String): Future[Unit] = ???

  override def save[T](key: String, value: T, expiration: Duration): Future[T] = ???
}
