package controllers

import play.api.mvc._
import module.file.FileModule

import controllers.common.requestArgsQuery._

object fileController extends Controller {
	def uploadFile = Action (request => uploadRequestArgs(request)(FileModule.uploadFile))
	def downloadFile(name : String) = Action ( Ok(FileModule.downloadFile(name)).as("image/png"))
}