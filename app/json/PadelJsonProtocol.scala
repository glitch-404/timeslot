package json

import model.CourtTime
import org.joda.time.format.{PeriodFormatter, PeriodFormatterBuilder}
import org.slf4j.{Logger, LoggerFactory}
import play.api.libs.json.{Json, Writes}
//import spray.json.{DefaultJsonProtocol, DeserializationException, JsObject, JsString, JsValue, RootJsonFormat}

/**
  * Defines the JSON protocol for spray so that
  * [[model.CourtTime]] objects can be encoded to JSON.
  */
object PadelJsonProtocol {

  val logger: Logger = LoggerFactory.getLogger(getClass)
  private lazy val periodFormatter: PeriodFormatter =
    new PeriodFormatterBuilder()
      .appendMinutes()
      .appendSuffix("min")
      .toFormatter

  implicit val courtTimeWrites: Writes[CourtTime] = (ct: CourtTime) => {
    Json.obj(
      "startTime" -> ct.startTime.toString,
      "duration" -> periodFormatter
        .print(ct.duration),
      "date"        -> ct.date.toString,
      "courtNumber" -> ct.courtNumber,
      "location"    -> ct.location
    )
  }
  /*
  implicit object CourtTimeJsonFormat extends RootJsonFormat[CourtTime] {
    private val nullStr = "null"
    def write(ct: CourtTime): JsObject = {
      logger.trace("Writing CourtTime to JSON object")
      JsObject(
        // TODO: Date and time formats.
        "startTime" -> JsString(ct.startTime.getOrElse(nullStr).toString()),
        "duration" -> JsString(periodFormatter.print(ct.duration.getOrElse(null))),
        "date" -> JsString(ct.date.getOrElse(nullStr).toString()),
        "courtNumber" -> JsString(ct.courtNumber),
        "location" -> JsString(ct.location.toString()),
      )
    }

    def read(value: JsValue) = {
      value.asJsObject().getFields("startTime", "duration", "date", "courtNumber", "location") match {
        case Seq(JsString(startTime), JsString(duration), JsString(date), JsString(courtNumber), JsString(location)) =>
          CourtTimeParser.toCourtTime(startTime, duration, date, courtNumber, location)
        case _ => throw new DeserializationException("Court time expected")
      }
    }
  }

   */
}
