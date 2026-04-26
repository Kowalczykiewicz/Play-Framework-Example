package controllers

import play.api.libs.ws.WSClient
import play.api.mvc._
import repository.Repository

import javax.inject._
import scala.concurrent.ExecutionContext

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class FormController @Inject()(
                                val controllerComponents: ControllerComponents,
                                repo: Repository,
                                ws: WSClient
                              )(implicit ec: ExecutionContext) extends BaseController {

  repo.init()

  def index = Action {
    Ok(views.html.index(repo.all()))
  }

  def convert = Action.async(parse.formUrlEncoded) { request =>
    val amount = request.body.get("amount").flatMap(_.headOption).map(_.toDouble).getOrElse(0.0)
    val target = request.body.get("target").flatMap(_.headOption).getOrElse("EUR")

    ws.url("https://open.er-api.com/v6/latest/USD").get().map { response =>
      val rate = (response.json \ "rates" \ target).as[Double]
      val result = amount * rate
      repo.insert(amount, target, result)
      Redirect(routes.FormController.index)
    }
  }

}
