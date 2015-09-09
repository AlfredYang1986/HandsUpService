package module.file

import play.api.libs.json.Json
import play.api.libs.json.Json.{toJson}
import play.api.libs.json.JsValue
import play.api.mvc.MultipartFormData
import play.api.libs.Files.TemporaryFile
import module.common.files.fop

object FileModule {
	def uploadFile(data : MultipartFormData[TemporaryFile]) : JsValue = fop.uploadFile(data)
	def downloadFile(name : String) : Array[Byte] = fop.downloadFile(name)
}