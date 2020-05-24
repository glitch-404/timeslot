package parsing

import model.CourtTime
import model.PadelCourts._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * Object responsible for scraping the CourtTime model objects from their corresponding URLs.
  * Uses hard coded URLs and enums, which is sufficient for this simplistic use case.
  */
object DataScraper {

  private val logger = Logger(getClass)
  private lazy val browser = JsoupBrowser()

  def courtsByDate(location: Location, date: String = "")(
    implicit ec: ExecutionContext
  ): Future[List[CourtTime]] = {
    logger.debug(s"datestring: $date")
    // TODO: Date validation?
    lazy val dateParam =
      if (date.isEmpty) DateParser.todayAsString else date
    lazy val courtFilter: PadelCourt => Boolean = { pc =>
      if (location.equals(All)) true else pc.location.equals(location)
    }
    allCourts
      .filter(courtFilter)
      .map(
        (pc: PadelCourt) =>
          parseCourts(pc.url.addParam("pvm", dateParam).toString(), pc.toString)
      )
      .toList
      .reduce[Future[List[CourtTime]]]((a, b) => a.zipWith(b)((x, y) => x ++ y))
  }

  private def parseCourts(url: String, location: String)(
    implicit ec: ExecutionContext
  ): Future[List[CourtTime]] = {
    logger.debug(s"Getting URL: $url")
    val elementList: Future[Iterable[Element]] = getAsyncUrl(url)
    elementList.map(iterableElements => {
      // Map Element to CourtDate
      iterableElements
        .filter(_.innerHtml.toLowerCase.contains("varaa"))
        .map(courtElement => {
          val linkOpt = courtElement >> element("a") >?> attr("href")
          CourtTimeParser.parseCourtElement(linkOpt, location)
        })
        .toList
    })
  }

  private def getAsyncUrl(
    url: String
  )(implicit ec: ExecutionContext): Future[Iterable[Element]] = {
    Future {
      browser.get(url) >> elementList(".t1b1111")
    }
  }

//  private def extractLink(availableCourt: Element): String = {
//    for {
//      linkOpt <- availableCourt >> element("a") >?> attr("href")
//      link <- linkOpt
//    } yield link
//  }
}
