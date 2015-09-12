package module.events

import play.api.libs.json.Json
import play.api.libs.json.Json.{toJson}
import play.api.libs.json.JsValue
import play.api.http.Writeable
import util.dao.from
import util.dao._data_connection
import util.errorcode.ErrorCode
import com.mongodb.casbah.Imports._
import module.sercurity.Sercurity

object CatchUpModule {
	def queryCatchUpEvents(data : JsValue) : JsValue = {
		
		val user_id = (data \ "user_id").asOpt[String].get
		val auth_token = (data \ "auth_token").asOpt[String].get
	
		var reVal : List[JsValue] = Nil
		from db() in "events" where ("catchs" -> user_id) select { iter => 
			var tmp : Map[String, JsValue] = Map.empty
			tmp += "event_id" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[String]("event_id").get)
			tmp += "title" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[String]("title").get)
			tmp += "detail" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[String]("detail").get)
			tmp += "date" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[Long]("date").get)
			tmp += "founder_id" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[String]("founder_id").get) 
			
			reVal = toJson(tmp) +: reVal
		}
	 
		Json.toJson(Map("status" -> toJson("ok"), "result" -> toJson(reVal)))
	}
	
	def catchUpEvent(data : JsValue) : JsValue = {
		val user_id = (data \ "user_id").asOpt[String].get
		val auth_token = (data \ "auth_token").asOpt[String].get
		val event_id = (data \ "event_id").asOpt[String].get
		
		val reVal = from db() in "events" where ("event_id" -> event_id) select (x => x)
		if (reVal.empty) ErrorCode.errorToJson("event not existing")
		else {

			val event = reVal.head
			event.getAs[MongoDBList]("catchs").map { lst => 
				if (!lst.exists(x => x.asInstanceOf[String].equals(user_id))) {
					lst.add(user_id)
					_data_connection.getCollection("events").update(DBObject("event_id" -> event_id), event)
				}
				queryCatchUpEvents(data)
				
			}.getOrElse(ErrorCode.errorToJson("Unknow error"))
		}
	}
}