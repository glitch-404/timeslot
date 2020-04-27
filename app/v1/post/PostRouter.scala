package v1.post

import controllers.PadelController
import javax.inject.Inject
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the PostResource controller.
  */
class PostRouter @Inject()(controller: PadelController) extends SimpleRouter {
  val prefix = "/padel/v1"

  def link(id: PostId): String = {
    import io.lemonlabs.uri.dsl._
    val url = prefix / id.toString
    url.toString()
  }

  override def routes: Routes = {
    case GET(p"/") =>
      controller.today

    case POST(p"/") =>
      controller.today

    case GET(p"/$date") =>
      controller.getByDate(date)
  }
}
