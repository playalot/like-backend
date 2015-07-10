package actors

import akka.actor._
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import play.api.libs.json.Json

import com.likeorz.event.LikeEvent
import com.likeorz.event.LikeEvent._

import scala.concurrent.Await
import scala.concurrent.duration._

@Singleton
class EventProducerActor @Inject() (configuration: Configuration) extends Actor with ActorLogging {

  var remotes = IndexedSeq.empty[ActorRef]
  var counter = 0

  override def preStart() = {
    // Discovery remote event consumer
    log.warning("Discovering remote event consumers...")
    configuration.getString("event-consumer.address").get.split(",").filter(_.nonEmpty).foreach { address =>
      try {
        val ref = Await.result(context.actorSelection(s"akka.tcp://LikeClusterSystem@$address/user/EventActor")
          .resolveOne(3.seconds), 3.seconds)
        context watch ref
        remotes = remotes :+ ref
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          log.warning(s"Remote actor[akka.tcp://LikeClusterSystem@$address/user/EventActor] didn't respond")
      }
    }
    log.warning("Remote actors: " + remotes.map(_.path.toString).mkString(","))
  }

  override def receive = {
    case event: LikeEvent if remotes.isEmpty =>
      log.info("No remote actors registered")
    case event: LikeEvent =>
      counter = (counter + 1) % remotes.size
      remotes(counter) forward Json.toJson(event).toString()
    case "STATUS" => sender() ! remotes.size
    case "JOIN" if !remotes.contains(sender()) =>
      log.warning(s"Remote actor[${sender()}] joined")
      context watch sender()
      remotes = remotes :+ sender()
    case Terminated(a) =>
      log.warning(s"Remote actor[$a] terminated")
      remotes = remotes.filterNot(_ == a)
    case _ =>
      log.warning("Remote actors: " + remotes.size)
      log.warning("Remote actors: " + remotes.map(_.toString()).mkString(","))
      log.warning("Receive invalid message")
  }

}

object EventProducerActor {
  val props = Props[EventProducerActor]
}