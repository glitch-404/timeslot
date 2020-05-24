package model

import io.lemonlabs.uri.Url

// PadelCourts namespace for holding court objects.
object PadelCourts {
  sealed case class PadelCourt(organization: Organization,
                               location: Location,
                               url: Url) {
    override def toString = s"$organization - $location"
  }
  // Case object 'enums' for type safety.
  sealed trait Organization
  case object PadelTampere extends Organization
  case object Padeluxe extends Organization

  sealed trait Location
  case object Linnakallio extends Location
  case object Messukylä extends Location
  case object Pirkkala extends Location
  case object All extends Location

  /**
    * IRL courts instantiated here. In the future, these could be read from e.g. a config file.
    */
  val allCourts: Set[PadelCourt] = Set(
    PadelCourt(
      PadelTampere,
      Linnakallio,
      Url.parse(
        "https://vj.slsystems.fi/padeltampere/ftpages/ft-varaus-table-01.php?laji=1"
      )
    ),
    PadelCourt(
      PadelTampere,
      Messukylä,
      Url.parse(
        "https://vj.slsystems.fi/padeltampere/ftpages/ft-varaus-table-01.php?laji=2"
      )
    ),
    PadelCourt(
      Padeluxe,
      Pirkkala,
      Url.parse(
        "https://vj.slsystems.fi/padeluxe/ftpages/ft-varaus-table-01.php?laji=1"
      )
    )
  )
}
