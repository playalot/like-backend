package com.likeorz

import akka.actor.ActorSystem
import akka.util.Timeout
import com.likeorz.actors.ClassifyActor.Tags
import com.likeorz.actors.MLJobActor.{ ReloadModel, RunKMeans }
import com.likeorz.actors.{ ClassifyActor, MLJobActor }
import org.apache.spark.SparkContext
import org.apache.spark.mllib.clustering.KMeansModel
import org.apache.spark.mllib.feature.Word2VecModel
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.pattern.ask
import scala.concurrent.ExecutionContext.Implicits.global

object LikeMLApp extends App {

  implicit val system = ActorSystem("LikeClusterSystem")
  implicit val timeout = Timeout(5.minutes)

  // Load w2v and k-means model
  val sc = new SparkContext("local[2]", "like-ml")

  var model = Word2VecModel.load(sc, "tag-w2v.model")
  var cluster = KMeansModel.load(sc, "tag-cluster.model")

  // Initialize actors
  val classifyActor = system.actorOf(ClassifyActor.props(model, cluster), "ClassifyActor")
  val mlJobActor = system.actorOf(MLJobActor.props(sc), "MLJobActor")

  println(Await.result(classifyActor.ask(Tags(Seq("明日香", "EVA", "景品", "傲娇么么哒", "其实是晒钱包的！", "小恶魔"))), 5.seconds))
  println(Await.result(classifyActor.ask(Tags(Seq("高达模型吧", "RG", "飞翼"))), 5.seconds))

  println(Await.result(mlJobActor.ask(RunKMeans).map(_.asInstanceOf[String]), 3.minutes))
  println(Await.result(mlJobActor.ask(ReloadModel).map(_.asInstanceOf[String]), 3.minutes))

  println(Await.result(classifyActor.ask(Tags(Seq("背面照", "这个应该是买不到了", "こんにちは、これは丸子", "miku", "ミク", "看心情发正面"))), 5.seconds))
  println(Await.result(classifyActor.ask(Tags(Seq("键盘", "漂亮", "OL", "黑丝好评", "好腿prpr", "凛凛蝶"))), 5.seconds))

  // Start http
  //  startHTTPServer()

  system.awaitTermination()

  def startHTTPServer(): Unit = {
    implicit val materializer = ActorMaterializer()

    val serverSource = Http().bind(interface = "localhost", port = 6635)

    val requestHandler: HttpRequest => HttpResponse = {
      case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
        HttpResponse(
          entity = HttpEntity(MediaTypes.`application/json`,
            """{"app":"like-ml"}"""))
      case _: HttpRequest => HttpResponse(404, entity = "Unknown resource!")
    }

    val bindingFuture: Future[Http.ServerBinding] =
      serverSource.to(Sink.foreach { connection => // foreach materializes the source
        //    println("Accepted new connection from " + connection.remoteAddress)
        connection handleWithSyncHandler requestHandler
      }).run()
  }

}
