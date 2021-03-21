package binding

import binding.DateRange.{defaultDuration, defaultEndTime, defaultStartTime}
import com.github.nscala_time.time.Imports.{LocalDate, LocalTime, Period}
import parsing.DateParser
import play.api.mvc.QueryStringBindable

case class DateRange(
    from: LocalDate,
    until: LocalDate,
    startTime: LocalTime = LocalTime.parse(defaultStartTime),
    endTime: LocalTime = LocalTime.parse(defaultEndTime),
    duration: Period = defaultDuration
)

object DateRange {

  private val defaultStartTime = "06:00"
  private val defaultEndTime   = "23:59"
  private val defaultDuration  = Period.minutes(90)

  implicit def queryStringBindable(
      implicit
      stringBinder: QueryStringBindable[String]): QueryStringBindable[DateRange] = {
    new QueryStringBindable[DateRange] {
      override def bind(
          key: String,
          params: Map[String, Seq[String]]
      ): Option[Either[String, DateRange]] = {
        for {
          from        <- stringBinder.bind("from", params)
          until       <- stringBinder.bind("until", params)
          startTime   <- stringBinder.bind("startTime", params)
          endTime     <- stringBinder.bind("endTime", params)
          minDuration <- stringBinder.bind("minDuration", params)
        } yield {
          (from, until, startTime, endTime, minDuration) match {
            case (Right(from), Right(until), Right(startTime), Right(endTime), Right(minDuration)) =>
              Right(toDateRange(from, until, startTime, endTime, minDuration))
            case (Right(from), Right(until), Left(_), Left(_), Left(_)) =>
              Right(toDateRange(from, until, defaultStartTime, defaultEndTime))
            case (Left(_), Right(until), Left(_), Left(_), Left(_)) =>
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

  private def toDateRange(
      from: String,
      until: String,
      startTime: String,
      endTime: String,
      minDuration: String = "01:30:00"
  ): DateRange = {
    DateRange(
      from = LocalDate.parse(from),
      until = LocalDate.parse(until),
      startTime = LocalTime.parse(startTime),
      endTime = LocalTime.parse(endTime),
      duration = Period.parse(minDuration, DateParser.getPeriodFormatter)
    )
  }
}
