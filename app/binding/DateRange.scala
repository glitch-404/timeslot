package binding

import play.api.mvc.QueryStringBindable

case class DateRange(from: String, until: String)

object DateRange {
  implicit def queryStringBindable(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[DateRange] =
    new QueryStringBindable[DateRange] {
      override def bind(
          key: String,
          params: Map[String, Seq[String]]
      ): Option[Either[String, DateRange]] = {
        for {
          from  <- stringBinder.bind("from", params)
          until <- stringBinder.bind("until", params)
        } yield {
          (from, until) match {
            case (Right(from), Right(until)) => Right(DateRange(from, until))
            case _                           => Left("Unable to bind a DateRange")
          }
        }
      }
      override def unbind(key: String, dateRange: DateRange): String = {
        stringBinder.unbind("from", dateRange.from) + "&" + stringBinder.unbind(
          "until",
          dateRange.until
        )
      }
    }
}
