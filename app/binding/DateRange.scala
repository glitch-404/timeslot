package binding

import binding.DateRange.{defaultEndTime, defaultStartTime}
import com.github.nscala_time.time.Imports.{LocalDate, LocalTime}
import parsing.DateParser
import play.api.mvc.QueryStringBindable

case class DateRange(
    from: LocalDate,
    until: LocalDate,
    startTime: LocalTime = LocalTime.parse(defaultStartTime),
    endTime: LocalTime = LocalTime.parse(defaultEndTime)
)

object DateRange {

  private val defaultStartTime = "06:00"
  private val defaultEndTime   = "23:59"

  implicit def queryStringBindable(implicit
      stringBinder: QueryStringBindable[String]
  ): QueryStringBindable[DateRange] = {
    new QueryStringBindable[DateRange] {
      override def bind(
          key: String,
          params: Map[String, Seq[String]]
      ): Option[Either[String, DateRange]] = {
        for {
          from      <- stringBinder.bind("from", params)
          until     <- stringBinder.bind("until", params)
          startTime <- stringBinder.bind("startTime", params)
          endTime   <- stringBinder.bind("endTime", params)
        } yield {
          (from, until, startTime, endTime) match {
            case (Right(from), Right(until), Right(startTime), Right(endTime)) =>
              Right(toDateRange(from, until, startTime, endTime))
            case (Right(from), Right(until), Left(msg1), Left(msg2)) =>
              Right(toDateRange(from, until, defaultStartTime, defaultEndTime))
            case (Left(msg1), Right(until), Left(msg2), Left(msg3)) =>
              Right(toDateRange(DateParser.todayAsString, until, defaultStartTime, defaultEndTime))
            case _ => Left("Missing mandatory parameter 'until' or unable to parse parameters!")
          }
        }
      }
      override def unbind(key: String, dateRange: DateRange): String = {
        stringBinder.unbind("from", dateRange.from.toString) + "&" + stringBinder.unbind(
          "until",
          dateRange.until.toString
        ) + "&" + stringBinder.unbind(
          "startTime",
          dateRange.startTime.toString
        ) + "&" + stringBinder.unbind(
          "endTime",
          dateRange.endTime.toString
        )
      }
    }
  }

  private def toDateRange(from: String, until: String, startTime: String, endTime: String): DateRange = {
    DateRange(
      from = LocalDate.parse(from),
      until = LocalDate.parse(until),
      startTime = LocalTime.parse(startTime),
      endTime = LocalTime.parse(endTime)
    )
  }
}
