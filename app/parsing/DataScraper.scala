package parsing

import binding.DateRange
import com.github.nscala_time.time.Imports.LocalTime
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
          courts    = parseCourts(targetUrl, pc.toString)
        } yield courts.map(timesBetween(_, dateRange.startTime, dateRange.endTime))
      }
      .reduce[Future[List[CourtTime]]]((a, b) => a.zipWith(b)((x, y) => (x ++ y).sorted))
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
        .sorted
    })
  }

  private def getAsyncUrl(
      url: String
  )(implicit ec: ExecutionContext): Future[Iterable[Element]] = {
    Future {
      browser.get(url) >> elementList(".t1b1111")
    }
  }

  private def timesBetween(courtTimes: List[CourtTime], startTime: LocalTime, endTime: LocalTime): List[CourtTime] = {
    courtTimes.filter { ct =>
      val courtStart = ct.startTime
      courtStart.isAfter(startTime) && courtStart.isBefore(endTime)
    }
  }

//  private def extractLink(availableCourt: Element): String = {
//    for {
//      linkOpt <- availableCourt >> element("a") >?> attr("href")
//      link <- linkOpt
//    } yield link
//  }
}
