package parsing

import binding.DateRange
import model.CourtTime
import model.PadelCourts._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

/**
  * Object responsible for scraping the CourtTime model objects from their corresponding URLs.
  * Uses hard coded URLs and enums, which is sufficient for this simplistic use case.
  */
object DataScraper {

  private val logger       = Logger(getClass)
  private lazy val browser = JsoupBrowser()

  def getRange(location: Location, dateRange: DateRange)(implicit ec: ExecutionContext): Future[List[CourtTime]] = {
    logger.trace(s"Range from: ${dateRange.from} until ${dateRange.until}")
    lazy val courtFilter: PadelCourt => Boolean = { pc =>
      if (location.equals(All)) true else pc.location.equals(location)
    }
    allCourts
      .filter(courtFilter)
      .flatMap { (pc: PadelCourt) =>
        val dates: List[String] = DateParser.getDateRange(dateRange)
        for {
          date <- dates
          targetUrl = pc.url.addParam("pvm", date).toString()
        } yield parseCourts(targetUrl, pc.toString)
      }
      .reduce[Future[List[CourtTime]]]((a, b) => a.zipWith(b)((x, y) => x ++ y))
  }

  private def parseCourts(
      url: String,
      location: String = DateParser.todayAsString
  )(implicit ec: ExecutionContext): Future[List[CourtTime]] = {
    logger.trace(s"Getting URL: $url")
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
