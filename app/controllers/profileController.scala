package controllers

import play.api._
import play.api.mvc._

import module.profile.ProfileModule
import controllers.common.requestArgsQuery.{requestArgs}

object profileController {
	def updateUserProfile = Action (request => requestArgs(request)(ProfileModule.updateUserProfile))
	def queryUserProfile = Action (request => requestArgs(request)(ProfileModule.queryUserProfile))
}