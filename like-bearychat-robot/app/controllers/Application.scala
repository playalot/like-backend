package controllers

import actors.AirActor
import org.joda.time.DateTime
import org.jsoup.Jsoup
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import akka.actor._
import javax.inject.Inject
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import play.api.libs.concurrent.Execution.Implicits._

import scalaj.http.Http

class Application @Inject() (system: ActorSystem) extends Controller {

  val airActor = system.actorOf(AirActor.props, "airActor")

  airActor ! AirActor.Update

  system.scheduler.schedule(
    (60 - DateTime.now.minuteOfHour().get()).minutes,
    180.minutes,
    airActor,
    AirActor.Update
  )

  def index = Action {
    Ok
  }

  def moegirl = Action(parse.json) { implicit request =>
    val word = (request.body \ "text").as[String].split(" ", 2)(1)
    try {
      val doc = Jsoup.connect("http://zh.moegirl.org/" + word).get()
      val text = doc.select("#mw-content-text p").toList.drop(1).map(_.text).mkString("\n")
      Ok(Json.obj(
        "text" -> s"http://zh.moegirl.org/$word \n $text"
      ).toString())
    } catch {
      case e: Throwable => Ok(Json.obj(
        "text" -> s"${word}词条没有找到～～"
      ))
    }
  }

  def tuling = Action(parse.json) { implicit request =>
    val question = (request.body \ "text").as[String].split(" ", 2)(1)
    val answer = Json.parse(Http("http://www.tuling123.com/openapi/api").params("key" -> "11cf72c69def10b00f433d0919f5c2a1", "info" -> question).asString.body)
    Ok(Json.obj(
      "text" -> (answer \ "text").asOpt[String]
    ).toString())
  }

}