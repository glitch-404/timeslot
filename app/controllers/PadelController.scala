package controllers

import javax.inject.Inject
import model.CourtTime
import binding.DateRange
import parsing.{DataScraper, DateParser}
import model.PadelCourts.All
import play.api.mvc._
import play.api.libs.json.Json
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.Await

class PadelController @Inject() (val controllerComponents: ControllerComponents) extends BaseController {

  import json.PadelJsonProtocol._

  private val logger = Logger(getClass)
  import scala.concurrent.ExecutionContext.Implicits.global

  def today: Action[AnyContent] =
    Action { implicit request: Request[AnyContent] =>
      val range = DateRange(from = DateParser.todayAsString, until = DateParser.todayAsString)
      val courtTimes: List[CourtTime] =
        Await.result(DataScraper.getRange(All, range), 5.seconds)
      Ok(Json.toJson(courtTimes))
    }

  def getByDate(date: String): Action[AnyContent] =
    Action { implicit request =>
      val range = DateRange(from = date, until = date)
      val courtTimes: List[CourtTime] =
        Await.result(DataScraper.getRange(All, range), 5.seconds)
      Ok(Json.toJson(courtTimes))
    }

  def getUntilDate(date: String): Action[AnyContent] =
    Action { implicit request =>
      val range = DateRange(from = DateParser.todayAsString, until = date)
      val courtTimes: List[CourtTime] =
        Await.result(DataScraper.getRange(All, range), 5.seconds)
      Ok(Json.toJson(courtTimes))
    }

  def getRange(dateRange: DateRange): Action[AnyContent] =
    Action { implicit request =>
      val courtTimes: List[CourtTime] =
        Await.result(DataScraper.getRange(All, dateRange), 5.seconds)
      Ok(Json.toJson(courtTimes))
    }

}
