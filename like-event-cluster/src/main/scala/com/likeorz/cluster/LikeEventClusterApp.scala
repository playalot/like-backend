package com.likeorz.cluster

import akka.routing.RoundRobinPool
import com.likeorz.common.JoinApiServer
import com.typesafe.config.ConfigFactory
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.Word2VecModel

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._

import scala.concurrent.Future

import com.likeorz.cluster.actors._

object LikeEventClusterApp extends App {

  val conf = ConfigFactory.load()
  implicit val system = ActorSystem("LikeClusterSystem")

  // Initialize event actor
  //  val eventActor = system.actorOf(EventActor.props, "EventActor")
  val eventRouter = system.actorOf(RoundRobinPool(5).props(EventActor.props), "EventRouter")
  val producers = conf.getString("event-producer.address").split(",").filterNot(_.isEmpty)
  // Discovery remote event producer
  println("Looking up remote event producers and register...")
  producers.foreach { address =>
    val ref = system.actorSelection(s"akka.tcp://like-api-server@$address/user/event-producer-actor")
    ref.tell(JoinApiServer, eventRouter)
  }
  // Initialize models
  val modelPath = conf.getString("ml.w2v-path")
  val clusterPath = conf.getString("ml.kmeans-path")

  var model: Word2VecModel = null
  var cluster: KMeansModel = null

  loadModel()

  // Initialize classify actors
  val classifyRouter = system.actorOf(RoundRobinPool(5).props(ClassifyActor.props(model, cluster)), "ClassifyRouter")
  //  val classifyActor = system.actorOf(ClassifyActor.props(model, cluster), "ClassifyActor")
  // Discovery remote event producer
  println("Looking up remote classify actors and register...")
  producers.foreach { address =>
    val ref = system.actorSelection(s"akka.tcp://like-api-server@$address/user/classification-actor")
    ref.tell(JoinApiServer, classifyRouter)
  }

  //  import akka.pattern.ask
  //  import scala.concurrent.duration._
  //  implicit val timeout = akka.util.Timeout(5.minutes)
  //  import scala.concurrent.ExecutionContext.Implicits.global
  //  classifyRouter.ask(com.likeorz.common.Tags(Seq("明日香", "EVA", "景品", "傲娇么么哒", "其实是晒钱包的！", "小恶魔"))).map(println)
  //  classifyRouter.ask(com.likeorz.common.Tags(Seq("EVA"))).map(println)
  //  classifyRouter.ask("EVA").map(println)
  //  println(Await.result(classifyActor.ask(Tags(Seq("明日香", "EVA", "景品", "傲娇么么哒", "其实是晒钱包的！", "小恶魔"))), 5.seconds))
  //  println(Await.result(classifyActor.ask(Tags(Seq("高达模型吧", "RG", "飞翼"))), 5.seconds))
  //  println(Await.result(classifyActor.ask(Tags(Seq("背面照", "这个应该是买不到了", "こんにちは、これは丸子", "miku", "ミク", "看心情发正面"))), 5.seconds))
  //  println(Await.result(classifyActor.ask(Tags(Seq("键盘", "漂亮", "OL", "黑丝好评", "好腿prpr", "凛凛蝶"))), 5.seconds))

  startHTTP()

  system.awaitTermination()

  def startHTTP(): Unit = {
    implicit val materializer = ActorMaterializer()

    val serverSource = Http().bind(interface = "localhost", port = 6635)

    val requestHandler: HttpRequest => HttpResponse = {
      case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
        HttpResponse(entity = HttpEntity(MediaTypes.`application/json`, """{"msg":"hello"}"""))
      case HttpRequest(GET, Uri.Path("/reloadModel"), _, _, _) =>
        loadModel()
        HttpResponse(entity = HttpEntity(MediaTypes.`application/json`, """{"msg":"hello"}"""))
      case _: HttpRequest => HttpResponse(404, entity = "Unknown resource!")
    }

    val bindingFuture: Future[Http.ServerBinding] =
      serverSource.to(Sink.foreach { connection => // foreach materializes the source
        connection handleWithSyncHandler requestHandler
      }).run()
  }

  def loadModel(): Unit = {
    println("load model!")
    def desObj[T](path: String): Option[T] = {
      import java.io._
      var in: ObjectInputStream = null
      try {
        in = new ObjectInputStream(new FileInputStream(path)) {
          override def resolveClass(desc: java.io.ObjectStreamClass): Class[_] = {
            try { Class.forName(desc.getName, false, getClass.getClassLoader) }
            catch { case ex: ClassNotFoundException => super.resolveClass(desc) }
          }
        }
        val obj = in.readObject().asInstanceOf[T]
        Some(obj)
      } catch {
        case e: Throwable =>
          e.printStackTrace()
          None
      } finally {
        if (in != null) in.close()
      }
    }

    model = desObj[Word2VecModel](modelPath).get
    cluster = desObj[KMeansModel](clusterPath).get
  }

}