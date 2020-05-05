package parsing

import model.CourtTime
import model.PadelCourts._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import play.api.Logger

/**
  * Object responsible for scraping the CourtTime model objects from their corresponding URLs.
  * Uses hard coded URLs and enums, which is sufficient for this simplistic use case.
  */
object DataScraper {

  private val logger = Logger(getClass)
  private lazy val browser = JsoupBrowser()

  def courtsByDate(location: Location, date: String = ""): List[CourtTime] = {
    logger.debug(s"datestring: $date")
    // TODO: Date validation?
    lazy val dateParam =
      if (date.isEmpty) DateParser.todayAsString else date
    lazy val courtFilter: PadelCourt => Boolean = pc =>
      if (location.equals(All)) true else pc.location.equals(location)
    allCourts
      .filter(courtFilter)
      .flatMap(
        pc =>
          parseCourts(pc.url.addParam("pvm", dateParam).toString(), pc.toString)
      )
      .toList
  }

  // This needs to return a Future
  private def parseCourts(url: String, location: String): List[CourtTime] = {
    logger.debug(s"Getting URL: $url")
    for {
      court: Element <- browser.get(url) >> elementList(".t1b1111")
      availableCourtOpt = if (court.innerHtml.toLowerCase.contains("varaa"))
        Some(court)
      else None
      linkOpt <- extractLink(availableCourtOpt)
      courtData = CourtTimeParser.parseCourtElement(linkOpt, location)
    } yield courtData
  }

  private def extractLink(availableCourt: Option[Element]): Option[String] = {
    for {
      linkOpt <- availableCourt >> element("a") >?> attr("href")
      link <- linkOpt
    } yield link
  }
}
