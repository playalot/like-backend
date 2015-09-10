package services

import javax.inject.{ Named, Inject, Singleton }

import akka.actor.{ ActorRef, ActorSystem }
import com.likeorz.common.PushUnreadLikes
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.duration._

@Singleton
class OnStartServiceImpl @Inject() (system: ActorSystem, @Named("push-likes-actor") pushLikesActor: ActorRef) extends OnStartService {

  Logger.info("Scheduled services started")
  system.scheduler.schedule(scala.math.abs(10 - DateTime.now.getMinuteOfHour).minutes, 10.minutes) {
    pushLikesActor ! PushUnreadLikes
  }

}
