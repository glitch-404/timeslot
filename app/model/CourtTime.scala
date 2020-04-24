package model

import com.github.nscala_time.time.Imports._

// TODO: Convert courtNumber and location to enums.
case class CourtTime(startTime: Option[LocalTime],
                     duration: Option[Duration],
                     date: Option[LocalDate],
                     courtNumber: String,
                     location: CourtLocation.Value)
