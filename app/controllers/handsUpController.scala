package controllers

import play.api.mvc._
import module.handsup.HandsUpModule

import controllers.common.requestArgsQuery._

object handsUpController extends Controller {
	def queryHandsUpEvents = Action (request => requestArgs(request)(HandsUpModule.queryHandsUpEvents))
	def postHandsUpEvent = Action (request => requestArgs(request)(HandsUpModule.postHandsUpEvent))
}