package controllers

import javax.inject.Inject
import parsing.DataScraper
import model.PadelCourts.All
import play.api.mvc._
import play.api.libs.json.Json
import play.api.Logger

class PadelController @Inject()(val controllerComponents: ControllerComponents)
    extends BaseController {

  import json.PadelJsonProtocol._

  private val logger = Logger(getClass)

  def today: Action[AnyContent] = Action { implicit request =>
    val courtTimes = DataScraper.courtsByDate(All)
    val r: Result = Ok(Json.toJson(courtTimes))
    r
  }

  def getByDate(date: String): Action[AnyContent] = Action { implicit request =>
    val courtTimes = DataScraper.courtsByDate(All, date)
    Ok(Json.toJson(courtTimes))
  }
}
