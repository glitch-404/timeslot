package parsing

import model.CourtTime
import com.github.nscala_time.time.Imports._
import io.lemonlabs.uri.Url
import org.slf4j.LoggerFactory

object CourtTimeParser {

  private val logger = LoggerFactory.getLogger(getClass())

  def parseCourtElement(link: Option[String], location: String): CourtTime = {
    if (link.isEmpty) throw new RuntimeException("Link missing from GET")
    logger.debug(s"link: $link")
    val (startTime, duration, date, courtNbr) = courtDataFromUrl(link.get) // Link can be empty string here.
    toCourtTime(startTime, duration, date, courtNbr, location)
  }

  def toCourtTime(startTime: String,
                  duration: String,
                  date: String,
                  courtNumber: String,
                  location: String): CourtTime = {
    CourtTime(
      DateParser.parseDateType[LocalTime](
        startTime,
        startTime => LocalTime.parse(startTime)
      ),
      DateParser.parseDateType[Period](
        duration,
        duration => Period.parse(duration, DateParser.getPeriodFormatter)
      ),
      DateParser.parseDateType[LocalDate](date, date => LocalDate.parse(date)),
      courtNumber,
      location
    )
  }

  // Parses a URL within the HTML table cell to gather court reservation data.
  private def courtDataFromUrl(
    link: String
  ): (String, String, String, String) = {
    val url = Url.parse(link)
    implicit val qp: Map[String, Vector[String]] = url.query.paramMap
    (
      getQueryParam("klo"),
      getQueryParam("kesto"),
      getQueryParam("pvm"),
      getQueryParam("res")
    )
  }

  private def getQueryParam(
    key: String
  )(implicit queryParams: Map[String, Vector[String]]) = {
    val value = queryParams.get(key)
    value.fold("")(paramList => paramList.head)
  }
}
