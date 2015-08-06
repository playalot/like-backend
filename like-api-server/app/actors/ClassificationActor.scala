package actors

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._

import com.likeorz.common._

import scala.concurrent.Await
import scala.concurrent.duration._

@Singleton
class ClassificationActor @Inject() (configuration: Configuration) extends Actor with ActorLogging {

  implicit val timeout = Timeout(3.seconds)
  var remotes = IndexedSeq.empty[ActorRef]
  var counter = 0

  override def preStart() = {
    // Discovery remote event consumer
    log.warning("Discovering remote event consumers...")
    configuration.getString("event-receiver.address").get.split(",").filter(_.nonEmpty).foreach { address =>
      try {
        val ref = Await.result(context.actorSelection(s"akka.tcp://LikeClusterSystem@$address/user/ClassifyRouter")
          .resolveOne(3.seconds), 3.seconds)
        context watch ref
        remotes = remotes :+ ref
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          log.warning(s"Remote actor[akka.tcp://LikeClusterSystem@$address/user/ClassifyRouter] didn't respond")
      }
    }
    log.warning("Remote actors: " + remotes.map(_.path.toString).mkString(","))
  }

  override def receive = {
    case post: ClassifyPost if remotes.isEmpty =>
      log.info("No remote actors registered")
    case post: ClassifyPost =>
      counter = (counter + 1) % remotes.size
      remotes(counter) forward post
    case tags: Tags if remotes.isEmpty =>
      log.info("No remote actors registered")
    case tags: Tags =>
      counter = (counter + 1) % remotes.size
      remotes(counter) forward tags
    case tag: Tag if remotes.isEmpty =>
      log.info("No remote actors registered")
    case tag: Tag =>
      counter = (counter + 1) % remotes.size
      remotes(counter) forward tag
    case JoinApiServer if !remotes.contains(sender()) =>
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

object ClassificationActor {
  val props = Props[ClassificationActor]
}