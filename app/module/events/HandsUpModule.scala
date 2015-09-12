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

object HandsUpModule {
	def queryHandsUpEvents(data : JsValue) : JsValue = {
	  
		val user_id = (data \ "user_id").asOpt[String].get
		val auth_token = (data \ "auth_token").asOpt[String].get

		/**
		 * query my event
		 */
		var reVal : List[JsValue] = Nil
		((from db() in "events" where ("founder_id" $eq user_id) select (x => x)).toList :: 
			(from db() in "events" where ("founder_id" $ne user_id) select (x => x)).toList).flatMap { x => x.map { iter =>
			
			var tmp : Map[String, JsValue] = Map.empty
			tmp += "event_id" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[String]("event_id").get)
			tmp += "title" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[String]("title").get)
			tmp += "detail" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[String]("detail").get)
			tmp += "date" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[Long]("date").get)
			tmp += "founder_id" -> toJson(iter.asInstanceOf[MongoDBObject].getAs[String]("founder_id").get) 
			
			reVal = toJson(tmp) +: reVal
		}}
		
		Json.toJson(Map("status" -> toJson("ok"), "result" -> toJson(reVal)))
	}
	
	def postHandsUpEvent(data : JsValue) : JsValue = {
	  
		val user_id = (data \ "user_id").asOpt[String].get
		val auth_token = (data \ "auth_token").asOpt[String].get
		
		val title = (data \ "title").asOpt[String].get
		val date = (data \ "date").asOpt[Long].get
	
		def createEventID(seed : String) : String = Sercurity.md5Hash(seed + Sercurity.getTimeSpanWithMillSeconds)
		
		val builder = MongoDBObject.newBuilder
		builder += "event_id" -> createEventID(title)
		builder += "title" -> title
		builder += "detail" -> title
		builder += "date" -> date
		builder += "founder_id" -> user_id
		
		val catchUp_lst = MongoDBList.newBuilder
		catchUp_lst += user_id
		
		builder += "catchs" -> catchUp_lst.result
	
		_data_connection.getCollection("events") += builder.result
		queryHandsUpEvents(data)
	}
}