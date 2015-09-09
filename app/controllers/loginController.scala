package controllers

import play.api._
import play.api.mvc._

import module.login.LoginModule
import controllers.common.requestArgsQuery.{requestArgs}

object loginController extends Controller {

	def index = Action {
		Ok(views.html.index("Your new application is ready."))
	}

	def authWithThird = Action (request => requestArgs(request)(LoginModule.authWithThird))
}