package parsing

import binding.DateRange
import com.github.nscala_time.time.Imports.{LocalDate, LocalTime, Period}
import model.CourtTime
import model.PadelCourts._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.joda.time.Minutes
import play.api.Logger

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

/**
  * Object responsible for scraping the CourtTime model objects from their corresponding URLs.
  * Uses hard coded URLs and enums, which is sufficient for this simplistic use case.
  */
object DataScraper {

  private val logger       = Logger(getClass)
  private lazy val browser = JsoupBrowser()
  val nonsenseSeed         = CourtTime(LocalTime.MIDNIGHT, Period.hours(3), LocalDate.nextYear(), 3, "All")

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
        } yield courts.map(applyTemporalFilters(_, dateRange))
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
          val maybeLink = courtElement >> element("a") >?> attr("href")
          CourtTimeParser.parseCourtElement(maybeLink, location)
        })
        .toList
        .sorted
    })
  }

  private def applyTemporalFilters(courts: List[CourtTime], dateRange: DateRange): List[CourtTime] = {
    val durationFiltered = timesBetween(courts, dateRange.startTime, dateRange.endTime)
    mergeCourtTimes(durationFiltered).filter(_.duration.getMinutes >= dateRange.duration.getMinutes)
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

  private def mergeCourtTimes(courts: List[CourtTime]): List[CourtTime] = {
    @tailrec
    def process(in: List[CourtTime], accum: List[CourtTime]): List[CourtTime] =
      in match {
        case x :: y :: ys if diff(y, x) == 0 => process(merge(y, x) :: ys, accum)
        case x :: xs                         => process(xs, x :: accum)
        case Nil                             => accum
      }

    process(courts, Nil).reverse
  }

  private def diff(first: CourtTime, second: CourtTime): Int = {
    if (!first.date.equals(second.date) ||
        first.courtNumber != second.courtNumber)
      Int.MaxValue

    if (first.startTime.isBefore(second.startTime)) {
      val firstEnd = first.startTime.plusMinutes(first.duration.getMinutes)
      Math.abs(Minutes.minutesBetween(firstEnd, second.startTime).getMinutes)
    } else {
      val secondEnd = second.startTime.plusMinutes(second.duration.getMinutes)
      Math.abs(Minutes.minutesBetween(secondEnd, first.startTime).getMinutes)
    }
  }

  private def merge(first: CourtTime, second: CourtTime): CourtTime = {
    if (first.startTime.isBefore(second.startTime)) {
      first.copy(duration = first.duration.plusMinutes(second.duration.getMinutes))
    } else {
      second.copy(duration = second.duration.plusMinutes(first.duration.getMinutes))
    }
  }
}
