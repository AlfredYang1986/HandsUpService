package module.profile

import play.api.libs.json.Json
import play.api.libs.json.Json.{toJson}
import play.api.libs.json.JsValue
import play.api.http.Writeable
import util.dao.from
import util.dao._data_connection
import util.errorcode.ErrorCode
import com.mongodb.casbah.Imports._

object ProfileModule {
	def updateUserProfile(data : JsValue) : JsValue = {
	 
		val user_id = (data \ "user_id").asOpt[String].get
		val auth_token = (data \ "auth_token").asOpt[String].get
	
		val reVal = from db() in "user_profile" where ("user_id" -> user_id) select (x => x)
		if (reVal.count == 0) {
			val builder = MongoDBObject.newBuilder
			("user_id" :: "screen_name" :: "screen_photo" :: "school" :: "decipline" :: Nil).foreach { x =>
				(data \ x).asOpt[String].map (value => builder += x -> value).getOrElse(builder += x -> "")
			}
			
			_data_connection.getCollection("user_profile") += builder.result
			Json.toJson(Map("status" -> toJson("ok"), "result" -> toJson(queryUserProfile2(data))))
			
		} else {

			val user = reVal.head
			("screen_name" :: "screen_photo" :: "school" :: "decipline" :: Nil).foreach { x =>
				(data \ x).asOpt[String].map (value => user += x -> value).getOrElse(user += x -> "")
			}
		  
			_data_connection.getCollection("user_profile").update(DBObject("user_id" -> user_id), user)
			Json.toJson(Map("status" -> toJson("ok"), "result" -> toJson(queryUserProfile2(data))))
		}
	}

	def queryUserProfile(data : JsValue) : JsValue = {
		Json.toJson(Map("status" -> toJson("ok"), "result" -> toJson(queryUserProfile2(data))))
	}
	
	private def queryUserProfile2(data : JsValue) : Map[String, JsValue] = {
		
		val user_id = (data \ "user_id").asOpt[String].get
		val auth_token = (data \ "auth_token").asOpt[String].get

		var reVal : Map[String, JsValue] = Map.empty
		from db() in "user_profile" where ("user_id" -> user_id) select { x => 
			("screen_name" :: "screen_photo" :: "school" :: "decipline" :: Nil).foreach { key =>
				x.getAs[String](key).map(value => reVal += key -> toJson(value)).getOrElse(Unit)
			}
		}
		
		reVal
	}
}