package com.likeorz

import akka.actor.ActorSystem
import scala.concurrent.Future
import scala.concurrent.duration._

import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._

import com.likeorz.actors._

object LikeEventClusterApp extends App {

  implicit val system = ActorSystem("LikeClusterSystem")

  val eventActor = system.actorOf(EventActor.props, "EventActor")

//  val pushNotificationActor = system.actorOf(PushNotificationActor.props, "PushNotificationActor")
//  system.actorOf(Props[LikeClusterListener], name = "LikeClusterListener")

  // Start http
  implicit val materializer = ActorMaterializer()

  val serverSource = Http().bind(interface = "localhost", port = 6635)

  val requestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) =>
      eventActor ! """{"test":"test"}"""
      HttpResponse(
        entity = HttpEntity(MediaTypes.`application/json`,
          """{"msg":"hello"}"""))
    case _: HttpRequest => HttpResponse(404, entity = "Unknown resource!")
  }

  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach { connection => // foreach materializes the source
    //    println("Accepted new connection from " + connection.remoteAddress)
    connection handleWithSyncHandler requestHandler
  }).run()
  // Start http end


  system.awaitTermination()
}