package parsing

import java.time.format.DateTimeFormatter

import com.github.nscala_time.time.Imports.DateTime
import org.joda.time.format.{
  DateTimeFormatterBuilder,
  PeriodFormatter,
  PeriodFormatterBuilder
}
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

trait DateParser {

  def parseDateType[D](x: String, typeTransformation: String => D): Option[D]

  def getPeriodFormatter: PeriodFormatter

  def todayAsString: String
}

object DateParser extends DateParser {

  private val logger = LoggerFactory.getLogger(getClass())

  private lazy val periodFormatter = new PeriodFormatterBuilder()
    .appendHours()
    .appendSuffix(":")
    .appendMinutes()
    .appendSuffix(":")
    .appendMillis()
    .toFormatter

  private lazy val dateFormatter = new DateTimeFormatterBuilder()
    .appendYear(4, 4)
    .appendLiteral('-')
    .appendMonthOfYear(2)
    .appendLiteral('-')
    .appendDayOfMonth(2)
    .toFormatter

  override def parseDateType[D](
      x: String,
      typeTransformation: String => D
  ): Option[D] = {
    logger.trace(s"Parsing $x")
    Try(typeTransformation(x)) match {
      case Success(parsedValue) => Some(parsedValue)
      case Failure(malformedValue) =>
        logger.error(s"Failed to parse $malformedValue"); None
    }
  }

  override def todayAsString: String = {
    val today = DateTime.now().toLocalDate
    today.toString(dateFormatter)
  }

  override def getPeriodFormatter: PeriodFormatter = periodFormatter
}
