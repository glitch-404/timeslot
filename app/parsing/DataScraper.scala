package parsing

import io.lemonlabs.uri.Url
import model.CourtTime
import model.Court
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import play.api.Logger

object DataScraper {

  private val logger = Logger(getClass)
  private lazy val browser = JsoupBrowser()

  private val baseUrls: Map[Court.Location, Url] = Map(
    Court.PadelTampere -> Url.parse(
      "https://vj.slsystems.fi/padeltampere/ftpages/ft-varaus-table-01.php" // ?laji=1
    ),
    Court.Padeluxe -> Url.parse(
      "https://vj.slsystems.fi/padeluxe/ftpages/ft-varaus-table-01.php?laji=1"
    )
  )

  def courtsByDate(location: Court.Location,
                   date: String = ""): List[CourtTime] = {
    logger.debug(s"datestring: $date")
    val dateParam =
      if (date.isEmpty) DateParser.todayAsString else date
    location match {
      case Court.All          => getAllCourts(dateParam)
      case Court.PadelTampere => getPadelTreCourts(dateParam)
      case Court.Padeluxe     => getPadeluxeCourts(dateParam)
    }
  }

  private def getAllCourts(date: String): List[CourtTime] = {
    getPadelTreCourts(date) ++ getPadeluxeCourts(date)
  }

  def getPadeluxeCourts(date: String): List[CourtTime] = {
    val padeluxeUrl = baseUrls(Court.Padeluxe)
      .addParam("pvm", date)
    parseCourts(padeluxeUrl.toString(), "Padeluxe")
  }

  // TODO: These need to be Futures.
  private def getPadelTreCourts(date: String): List[CourtTime] = {
    val messukyläUrl = baseUrls(Court.PadelTampere)
      .addParam("laji", "2")
      .addParam("pvm", date)
    val linnakallioUrl = baseUrls(Court.PadelTampere)
      .addParam("laji", "1")
      .addParam("pvm", date)
    val messukyläTimes =
      parseCourts(messukyläUrl.toString(), "Padel Tampere - Messukylä")
    val linnakallioTimes =
      parseCourts(linnakallioUrl.toString(), "Padel Tampere - Linnakallio")
    messukyläTimes ++ linnakallioTimes
  }

  private def parseCourts(url: String, location: String): List[CourtTime] = {
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
