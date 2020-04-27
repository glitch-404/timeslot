package controllers

import javax.inject.Inject
import model.CourtTime
import parsing.DateParser
import play.api.mvc._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import play.api.libs.json.Json
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Element
import io.lemonlabs.uri.Url
import parsing.CourtTimeParser
import play.api.Logger

class PadelController @Inject()(val controllerComponents: ControllerComponents)
    extends BaseController {

  import json.PadelJsonProtocol._

  private val logger = Logger(getClass)
  private lazy val browser = JsoupBrowser()
  private val baseUrl: Url =
    Url.parse(
      "https://vj.slsystems.fi/padeltampere/ftpages/ft-varaus-table-01.php?goto=0&laji=1"
    )

  def today: Action[AnyContent] = Action { implicit request =>
    val courtUrl = courtUrlByDate()
    logger.debug(s"court URL: ${courtUrl.toString()}")
    val elementList = parseCourts(courtUrl.toString())
    val r: Result = Ok(Json.toJson(elementList))
    r
  }

  def getByDate(date: String): Action[AnyContent] = Action { implicit request =>
    logger.debug(s"inputStr $date")
    val courtUrl = courtUrlByDate(date)
    logger.debug(s"court URL: ${courtUrl.toString()}")
    val elementList = parseCourts(courtUrl.toString())
    Ok(Json.toJson(elementList))
  }

  // TODO: varattujen käsittely
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

  // TODO: Validation for dates at some point
  private def courtUrlByDate(dateString: String = ""): Url = {
    logger.debug(s"datestring: $dateString")
    val dateParam =
      if (dateString.isEmpty) DateParser.todayAsString else dateString
    baseUrl.addParam("pvm", dateParam)
  }
}
