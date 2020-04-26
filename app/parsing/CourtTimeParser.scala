package parsing

import model.{CourtLocation, CourtTime}
import com.github.nscala_time.time.Imports._
import com.github.nscala_time.time.StaticLocalDate
import io.lemonlabs.uri.Url
import net.ruippeixotog.scalascraper.model.Element
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import org.joda.time.format.{PeriodFormatter, PeriodFormatterBuilder}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object CourtTimeParser {

  val logger = LoggerFactory.getLogger(getClass())

  private lazy val formatter: PeriodFormatter = new PeriodFormatterBuilder()
    .appendHours()
    .appendSuffix(":")
    .appendMinutes()
    .appendSuffix(":")
    .appendMillis()
    .toFormatter

  def parseCourtElement(link: String): CourtTime = {
    logger.debug(s"link: $link")
    val (startTime, duration, date, courtNbr) = parseUrl(link) // Link can be empty string here.
    toCourtTime(startTime, duration, date, courtNbr, "PadelTampere") // Hard code for now
  }

  def toCourtTime(startTime: String, duration: String, date: String, courtNumber: String, location: String): CourtTime = {
    CourtTime(
      parseDataType[LocalTime](startTime, startTime => LocalTime.parse(startTime)),
      parseDataType[Period](duration, duration => Period.parse(duration, formatter)),
      parseDataType[LocalDate](date, date => LocalDate.parse(date)),
      courtNumber,
      CourtLocation.withName(location)
    )
  }

  private def parseUrl(link: String): (String, String, String, String) = {
    val url = Url.parse(link)
    implicit val qp: Map[String, Vector[String]] = url.query.paramMap
    (getQueryParam("klo"), getQueryParam("kesto"), getQueryParam("pvm"), getQueryParam("res"))
  }

  private def getQueryParam(key: String)(implicit queryParams: Map[String, Vector[String]]) = {
    val value = queryParams.get(key)
    value.fold("")(paramList => paramList.head)
  }

  private def parseDataType[T](x: String, typeTransformation: (String)=>T): Option[T] = {
    logger.debug(s"Parsing $x")
    Try(typeTransformation(x)) match {
      case Success(parsedValue) => Some(parsedValue)
      case Failure(malformedValue) => logger.error(s"Failed to parse $malformedValue"); None
    }
  }
}