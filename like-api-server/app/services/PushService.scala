package services

import com.likeorz.push.{ JPushNotification, PushNotification }
import play.api.libs.json.{ JsObject, Json }

import scala.concurrent.Future

trait PushService {

  def sendPushNotification(push: PushNotification): Unit

  def sendPushNotificationViaJPush(notification: JPushNotification): Unit

  def sendPushNotificationToUser(userId: Long, alert: String, badge: Int, extra: JsObject = Json.obj()): Future[Unit]

}
