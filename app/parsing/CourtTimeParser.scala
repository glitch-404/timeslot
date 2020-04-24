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

  def parseCourtElement(link: String): CourtTime = {
    logger.info(s"link: $link")
    val (startTime, duration, date, courtNbr) = parseUrl(link) // Link can be empty string here.
    CourtTime(
      parseDataType[LocalTime](startTime, startTime => LocalTime.parse(startTime)),
      parseDataType[Duration](duration, duration => Duration.parse(duration)),
      parseDataType[LocalDate](date, date => LocalDate.parse(date)),
      courtNbr,
      CourtLocation.PadelTampere // Hard code for now
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
    logger.debug("Parsing {}", x)
    Try(typeTransformation(x)) match {
      case Success(parsedValue) => Some(parsedValue)
      case Failure(malformedValue) => logger.error("Failed to parse {}", malformedValue); None
    }
  }
}