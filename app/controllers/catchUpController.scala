package controllers

import play.api.mvc._
import module.events.CatchUpModule

import controllers.common.requestArgsQuery._

object catchUpController {
	def queryCatchUpEvents = Action (request => requestArgs(request)(CatchUpModule.queryCatchUpEvents))
	def catchUpEvent = Action (request => requestArgs(request)(CatchUpModule.catchUpEvent))
}