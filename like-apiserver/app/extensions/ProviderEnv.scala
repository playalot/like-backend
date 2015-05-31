package extensions

import com.mohiva.play.silhouette.api.Provider

/**
 * Created by Guan Guan
 * Date: 5/29/15
 */
trait ProviderEnv {
  def providers: Map[String, Provider]
}

object ProviderEnv {
  def apply(providersImpl: Map[String, Provider]) = new ProviderEnv {
    override def providers: Map[String, Provider] = providersImpl
  }
}
