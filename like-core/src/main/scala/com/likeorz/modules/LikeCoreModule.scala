package com.likeorz.modules

import akka.routing.RoundRobinPool
import com.google.inject.AbstractModule
import com.likeorz.actors._
import com.likeorz.services._
import com.likeorz.services.store.{ RedisService, MongoDBService }
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport

class LikeCoreModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  override def configure(): Unit = {
    // Other stores
    bind[MongoDBService]
    bind[RedisService]

    bind[PushService]

    // DB services
    bind[InfoService]
    bind[UserFollowService]
    bind[UserSettingService]
    bind[TagService].to[TagServiceImpl]
    bind[PostService].to[PostServiceImpl]
    bind[UserService].to[UserServiceImpl]
    bind[MarkService].to[MarkServiceImpl]
    bind[PromoteService].to[PromoteServiceImpl]
    bind[NotificationService].to[NotificationServiceImpl]

    // Event bus service and actors
    bind[EventBusService]
    bindActor[EventLogSubscriber]("event-log-subscriber", RoundRobinPool(5).props)
    bindActor[PublishEventSubscriber]("publish-event-subscriber", RoundRobinPool(5).props)
    bindActor[MarkEventSubscriber]("mark-event-subscriber", RoundRobinPool(5).props)
    bindActor[RecommendToAllEventSubscriber]("recommend-to-all-event-subscriber", RoundRobinPool(5).props)
    bindActor[RecommendToGroupEventSubscriber]("recommend-to-group-event-subscriber", RoundRobinPool(5).props)

    bindActor[PushNotificationActor]("push-notification-actor")
  }

}
