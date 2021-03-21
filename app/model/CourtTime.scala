package model

import com.github.nscala_time.time.Imports._

/**
  * Data model representation of court reservations.
  *
  * @param startTime The starting time of the court reservation
  * @param duration The duration of the reservation
  * @param date The date of the reservation
  * @param courtNumber The court number of the reservation
  * @param location The location (first Padel Tampere, then also including Padeluxe)
  */
case class CourtTime(startTime: LocalTime, duration: Period, date: LocalDate, courtNumber: Int, location: String)
    extends Ordered[CourtTime] {
  override def compare(that: CourtTime): Int = {
    courtNumber.compareTo(that.courtNumber) match {
      case 0 =>
        date.compareTo(that.date) match {
          case 0     => startTime.compareTo(that.startTime)
          case other => other
        }
      case other => other
    }
  }
}
