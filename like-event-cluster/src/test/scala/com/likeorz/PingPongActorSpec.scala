package com.likeorz

import akka.actor.ActorSystem
import akka.actor.Actor
import akka.actor.Props
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import com.likeorz.cluster.actors.{PushNotificationActor$, SettleLikesActor$}
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll

class PingPongActorSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {

  def this() = this(ActorSystem("MySpec"))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "A Ping actor" must {
    "send back a ping on a pong" in {
      val pingActor = system.actorOf(SettleLikesActor.props)
      pingActor ! PushNotificationActor.PongMessage("pong")
      expectMsg(SettleLikesActor.PingMessage("ping"))
    }
  }

  "A Pong actor" must {
    "send back a pong on a ping" in {
      val pongActor = system.actorOf(PushNotificationActor.props)
      pongActor ! SettleLikesActor.PingMessage("ping")
      expectMsg(PushNotificationActor.PongMessage("pong"))
    }
  }

}
