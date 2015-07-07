package actors

import akka.actor.{ Props, ActorLogging, Actor }
import play.api.libs.json.{ JsArray, Json }

import scalaj.http._

class AirActor extends Actor with ActorLogging {
  import AirActor._

  def receive = {
    case Update =>
      log.info("Report weather...")
      try {
        val jsonArr = Json.parse(Http("http://www.pm25.in/api/querys/pm2_5.json").params("city" -> "北京", "token" -> "5j1znBVAsnSf5xQyNQyq").asString.body).as[JsArray]
        val info = jsonArr.value.filter(v => (v \ "position_name").asOpt[String] == Some("东四")).head

        //        val info = Json.parse(
        //          """
        //            |{"aqi":0,"area":"北京","pm2_5":0,"pm2_5_24h":0,"position_name":"农展馆","primary_pollutant":null,"quality":null,"station_code":"1005A","time_point":"2015-05-26T10:00:00Z"}
        //          """.stripMargin)

        log.info(info.toString())

        val rand = scala.util.Random.nextInt(10000) + 1000
        val post = Json.parse(Http("http://api.likeorz.com/v2/post/" + rand).asString.body)

        val weatherJson = Json.obj(
          "text" -> s"PM25 : ${(info \ "pm2_5").as[Int]} \t|\t 主要污染 : ${(info \ "primary_pollutant").asOpt[String].getOrElse("")}    |    空气质量 : ${(info \ "quality").asOpt[String].getOrElse("")}",
          "attachments" -> Json.arr(Json.obj(
            "title" -> "like - 你的新玩具",
            "color" -> "#ffa500",
            "images" -> Json.arr(Json.obj("url" -> (post \ "data" \ "content").asOpt[String]))
          ))
        )
        val result = Http("https://hook.bearychat.com/=bw6k6/incoming/06da0345573a82dbd9f0c58d7b5072b4").postData(weatherJson.toString).header("content-type", "application/json").asString.code
        println(result)
      } catch {
        case e: Throwable => e.printStackTrace()
      }
    case _ =>
  }
}

object AirActor {
  val props = Props[AirActor]
  case object Update
}