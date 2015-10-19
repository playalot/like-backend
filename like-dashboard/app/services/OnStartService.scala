package services

import javax.inject.{ Inject, Singleton }

import akka.actor.ActorSystem
import com.likeorz.utils.{ KeyUtils, RedisCacheClient }
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._

@Singleton
class OnStartService @Inject() (system: ActorSystem) {

  Logger.info("Scheduled services started")
  // Zero daily ranks at midnight
  system.scheduler.schedule((24.hours.toMinutes - new DateTime().getMinuteOfDay).minutes, 24.hours) {
    RedisCacheClient.rename(KeyUtils.likeRankToday, KeyUtils.likeRankYesterday)
  }

}
