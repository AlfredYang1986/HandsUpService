package module.login

import play.api.libs.json.Json
import play.api.libs.json.Json.{toJson}
import play.api.libs.json.JsValue
import play.api.http.Writeable
import util.dao.from
import util.dao._data_connection
import util.errorcode.ErrorCode
import com.mongodb.casbah.Imports._
import module.sercurity.Sercurity
//import module.sms._

object LoginModule {

	def authWithThird(data : JsValue) : JsValue = {
	
		def createUserID(seed : String) : String = Sercurity.md5Hash(seed + Sercurity.getTimeSpanWithMillSeconds)
		def createUserAuthToken(seed : String) : String = Sercurity.md5Hash(seed + Sercurity.getTimeSpanWithMillSeconds)
	  
		val provider_name = (data \ "provider_name").asOpt[String].get
		val provider_open_id = (data \ "provider_open_id").asOpt[String].get
		val provider_auth_token = (data \ "provider_auth_token").asOpt[String].get
	
		val reVal = from db() in "users" where ("SNS.provider_name" -> provider_name, "SNS.provider_open_id" -> provider_open_id) select (x => x)
		if (reVal.empty) {
			val user_id = createUserID(provider_name + provider_open_id)
			val auth_token = createUserID(provider_name + provider_open_id)
			val builder = MongoDBObject.newBuilder
			builder += "user_id" -> user_id
			builder += "auth_token" -> auth_token
			
			val sns_lst = MongoDBList.newBuilder
			
			val sns_builder = MongoDBObject.newBuilder
			sns_builder += "provider_name" -> provider_name
			sns_builder += "provider_open_id" -> provider_open_id
			sns_builder += "provider_auth_token" -> provider_auth_token
			(data \ "provider_refresh_token").asOpt[String].map(x => sns_builder += "provider_name" -> x).getOrElse(Unit)
			
			sns_lst += sns_builder.result
			
			builder += "SNS" -> sns_lst.result
		
			_data_connection.getCollection("users") += builder.result
			
			Json.toJson(Map("status" -> toJson("ok"), "result" -> toJson(Map("user_id" -> user_id, "auth_token" -> auth_token))))
			
		} else {
			
			val user = reVal.head
			val user_id = user.getAs[String]("user_id").get
			val auth_token = user.getAs[String]("auth_token").get
	
			user.getAs[MongoDBList]("SNS").map ( lst => lst.foreach { iter => 
//				if (iter.asInstanceOf[BasicDBObject].getAs[String]("provider_name").get.endsWith(provider_name) && 
//					iter.asInstanceOf[BasicDBObject].getAs[String]("provider_open_id").get.endsWith(provider_open_id)) {

					iter.asInstanceOf[BasicDBObject] += "provider_auth_token" -> provider_auth_token
					(data \ "provider_refresh_token").asOpt[String].map(x => iter.asInstanceOf[BasicDBObject] += "provider_name" -> x).getOrElse(Unit)

					_data_connection.getCollection("users").update(DBObject("user_id" -> user_id), user)
//				}
			})

			Json.toJson(Map("status" -> toJson("ok"), "result" -> toJson(Map("user_id" -> user_id, "auth_token" -> auth_token))))
		}
	}
}