package controllers

import javax.inject.Inject
import model.CourtTime
import play.api.mvc._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import play.api.libs.json.Json
//import spray.json._
import com.github.nscala_time.time.Imports._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element
import play.api.libs.json.{JsPath, Reads, Writes}
//import org.joda.time.format.{PeriodFormatter, PeriodFormatterBuilder}
import parsing.CourtTimeParser

import scala.concurrent._

class PadelController @Inject()(val controllerComponents: ControllerComponents)
    extends BaseController {

  import json.PadelJsonProtocol._

  private lazy val browser = JsoupBrowser()

  def today: Action[AnyContent] = Action { implicit request =>
    val courtUrl =
      "https://vj.slsystems.fi/padeltampere/ftpages/ft-varaus-table-01.php?laji=1&pvm=2020-04-16&goto=0"
    val elementList = parseCourts(courtUrl)
    val r: Result = Ok(Json.toJson(elementList))
    r
  }

  // TOOD: varattujen käsittely
  def parseCourts(url: String): List[CourtTime] = {
    for {
      court: Element <- browser.get(url) >> elementList(".t1b1111")
      availableCourtOpt = if (court.innerHtml.toLowerCase.contains("varaa"))
        Some(court)
      else None
      linkOpt <- extractLink(availableCourtOpt)
      courtData = CourtTimeParser.parseCourtElement(linkOpt)
    } yield courtData
  }

  def extractLink(availableCourt: Option[Element]): Option[String] = {
    for {
      linkOpt <- availableCourt >> element("a") >?> attr("href")
      link <- linkOpt
    } yield link
  }

  //  def asyncIndex: Action[AnyContent] = Action.async { implicit request =>
  //    val r: Future[Result] = Future.successful(Ok("hello world"))
  //    r
  //  }

  //  private def courtTimeToString(courtTime: CourtTime): String = {
//      val formatter: PeriodFormatter = new PeriodFormatterBuilder()
//        .appendHours()
//        .appendSuffix("h")
//        .toFormatter
  //    //    val durationStr = formatter.print(courtTime.duration.toPeriod)
  //    s"CourtTime(Start time: ${courtTime.startTime}, Duration: ${courtTime.duration}, Date: ${courtTime.date}, Court: ${courtTime.courtNumber}, Location: ${courtTime.location})"
  //  }
}
