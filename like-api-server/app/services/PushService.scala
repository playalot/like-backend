package services

import com.likeorz.push.PushNotification
import play.api.libs.json.{ Json, JsObject }

import scala.concurrent.Future

/**
 * Created by Guan Guan
 * Date: 6/23/15
 */
trait PushService {

  def sendPushNotification(push: PushNotification): Unit

  def sendRemotePushNotification(push: PushNotification): Unit

  def sendPushNotificationToUser(userId: Long, alert: String, badge: Int, extra: JsObject = Json.obj()): Future[Unit]

}
