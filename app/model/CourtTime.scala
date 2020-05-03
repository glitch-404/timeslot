package model

import com.github.nscala_time.time.Imports._

/**
  *
  * @param startTime The starting time of the court reservation
  * @param duration The duration of the reservation
  * @param date The date of the reservation
  * @param courtNumber The court number of the reservation
  * @param location The location (first Padel Tampere, then also including Padeluxe)
  */
case class CourtTime(startTime: Option[LocalTime],
                     duration: Option[Period],
                     date: Option[LocalDate],
                     courtNumber: String,
                     location: String)
