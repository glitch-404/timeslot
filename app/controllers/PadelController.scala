package controllers

import javax.inject.Inject
import model.CourtTime
import parsing.DataScraper
import model.PadelCourts.All
import play.api.mvc._
import play.api.libs.json.Json
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.{Failure, Success}

class PadelController @Inject()(val controllerComponents: ControllerComponents)
    extends BaseController {

  import json.PadelJsonProtocol._

  private val logger = Logger(getClass)
  import scala.concurrent.ExecutionContext.Implicits.global

  def today: Action[AnyContent] =
    Action { implicit request =>
      val courtTimes: List[CourtTime] =
        Await.result(DataScraper.courtsByDate(All), 5.seconds)
      Ok(Json.toJson(courtTimes))
    }

  def getByDate(date: String): Action[AnyContent] =
    Action { implicit request =>
      val courtTimes: List[CourtTime] =
        Await.result(DataScraper.courtsByDate(All, date), 5.seconds)
      Ok(Json.toJson(courtTimes))
    }

  def getUntilDate(date: String): Action[AnyContent] =
    Action { implicit request =>
      val courtTimes: List[CourtTime] =
        Await.result(DataScraper.courtsByDate(All, date), 5.seconds)
      Ok(Json.toJson(courtTimes))
    }
}
