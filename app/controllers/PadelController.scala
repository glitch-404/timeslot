package controllers

import javax.inject.Inject
import play.api.mvc._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

import scala.concurrent._

class PadelController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  def index1: Action[AnyContent] = Action { implicit request =>
    val browser = JsoupBrowser()
    val padel = browser.get("https://vj.slsystems.fi/padeltampere/")
    val r: Result = Ok(padel.toString)
    r
  }

  def asyncIndex: Action[AnyContent] = Action.async { implicit request =>
    val r: Future[Result] = Future.successful(Ok("hello world"))
    r
  }


}