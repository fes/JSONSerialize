/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/


package com.fesLabs.web.json.mongo;

import com.fesLabs.web.json.JsonArray;
import com.fesLabs.web.json.JsonBoolean;
import com.fesLabs.web.json.JsonClass;
import com.fesLabs.web.json.JsonNumber;
import com.fesLabs.web.json.JsonString;
import com.fesLabs.web.json.JsonValue;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class MongoConvert {

	public static DBObject jsonClassToDBObject(JsonClass jsonClass) {
		DBObject dbObject = new BasicDBObject();
		for (String name : jsonClass.getMembers().keySet()) {
			JsonValue jsonObject = jsonClass.getMembers().get(name);
			if (jsonObject instanceof JsonClass) {
				JsonClass jsonValue = (JsonClass) jsonObject;
				DBObject childObject = jsonClassToDBObject(jsonValue);
				dbObject.put(name, childObject);
			} else if (jsonObject instanceof JsonArray) {
				JsonArray jsonValue = (JsonArray) jsonObject;
				BasicDBList childObject = jsonArrayToDBList(jsonValue);
				dbObject.put(name, childObject);
			} else if (jsonObject instanceof JsonString) {
				JsonString jsonValue = (JsonString) jsonObject;
				dbObject.put(name, jsonValue.getValue());
			} else if (jsonObject instanceof JsonNumber) {
				JsonNumber jsonValue = (JsonNumber) jsonObject;
				if (jsonValue.isIsDouble()) {
					dbObject.put(name, jsonValue.getAsDouble());
				} else {
					dbObject.put(name, jsonValue.getAsLong());
				}
			} else if (jsonObject instanceof JsonBoolean) {
				JsonBoolean jsonValue = (JsonBoolean) jsonObject;
				dbObject.put(name, jsonValue.getValue());
			}
		}
		return dbObject;
	}

	public static BasicDBList jsonArrayToDBList(JsonArray jsonArray) {
		BasicDBList dbObject = new BasicDBList();
		for(JsonValue jsonObject : jsonArray.getMembers()) {
			if (jsonObject instanceof JsonClass) {
				JsonClass jsonValue = (JsonClass) jsonObject;
				DBObject childObject = jsonClassToDBObject(jsonValue);
				dbObject.add(childObject);
			} else if (jsonObject instanceof JsonArray) {
				JsonArray jsonValue = (JsonArray) jsonObject;
				BasicDBList childObject = jsonArrayToDBList(jsonValue);
				dbObject.add(childObject);
			} else if (jsonObject instanceof JsonString) {
				JsonString jsonValue = (JsonString) jsonObject;
				dbObject.add(jsonValue.getValue());
			} else if (jsonObject instanceof JsonNumber) {
				JsonNumber jsonValue = (JsonNumber) jsonObject;
				if (jsonValue.isIsDouble()) {
					dbObject.add(jsonValue.getAsDouble());
				} else {
					dbObject.add(jsonValue.getAsLong());
				}
			} else if (jsonObject instanceof JsonBoolean) {
				JsonBoolean jsonValue = (JsonBoolean) jsonObject;
				dbObject.add(jsonValue.getValue());
			}
		}
		return dbObject;
	}

	public static JsonClass DBObjectToJson(BasicDBObject dbObject) {
		JsonClass jsonClass = new JsonClass();
		for (String key : dbObject.keySet()) {
			Object oValue = dbObject.get(key);
			if (oValue instanceof Byte) {
				JsonNumber newValue = new JsonNumber((Byte) oValue);
				jsonClass.add(key, newValue);
			} else if (oValue instanceof Short) {
				JsonNumber newValue = new JsonNumber((Short) oValue);
				jsonClass.add(key, newValue);
			} else if (oValue instanceof Integer) {
				JsonNumber newValue = new JsonNumber((Integer) oValue);
				jsonClass.add(key, newValue);
			} else if (oValue instanceof Long) {
				JsonNumber newValue = new JsonNumber((Long) oValue);
				jsonClass.add(key, newValue);
			} else if (oValue instanceof Double) {
				JsonNumber newValue = new JsonNumber((Double) oValue);
				jsonClass.add(key, newValue);
			} else if (oValue instanceof Float) {
				JsonNumber newValue = new JsonNumber((Float) oValue);
				jsonClass.add(key, newValue);
			} else if (oValue instanceof Boolean) {
				JsonBoolean newValue = new JsonBoolean((Boolean) oValue);
				jsonClass.add(key, newValue);
			} else if (oValue instanceof String) {
				JsonString newValue = new JsonString((String) oValue);
				jsonClass.add(key, newValue);
			} else if (oValue instanceof BasicDBList) {
				JsonArray newValue = dbListToJsonArray((BasicDBList)oValue);
				jsonClass.add(key, newValue);
			} else if (oValue instanceof BasicDBObject) {
				JsonClass newValue = DBObjectToJson((BasicDBObject) oValue);
				jsonClass.add(key, newValue);
			}
		}
		return jsonClass;
	}

	public static JsonArray dbListToJsonArray(BasicDBList dbList) {
		JsonArray jsonArray = new JsonArray();
		for(Object oValue : dbList) {
			if (oValue instanceof Byte) {
				JsonNumber newValue = new JsonNumber((Byte) oValue);
				jsonArray.add(newValue);
			} else if (oValue instanceof Short) {
				JsonNumber newValue = new JsonNumber((Short) oValue);
				jsonArray.add(newValue);
			} else if (oValue instanceof Integer) {
				JsonNumber newValue = new JsonNumber((Integer) oValue);
				jsonArray.add(newValue);
			} else if (oValue instanceof Long) {
				JsonNumber newValue = new JsonNumber((Long) oValue);
				jsonArray.add(newValue);
			} else if (oValue instanceof Double) {
				JsonNumber newValue = new JsonNumber((Double) oValue);
				jsonArray.add(newValue);
			} else if (oValue instanceof Float) {
				JsonNumber newValue = new JsonNumber((Float) oValue);
				jsonArray.add(newValue);
			} else if (oValue instanceof Boolean) {
				JsonBoolean newValue = new JsonBoolean((Boolean) oValue);
				jsonArray.add(newValue);
			} else if (oValue instanceof String) {
				JsonString newValue = new JsonString((String) oValue);
				jsonArray.add(newValue);
			} else if (oValue instanceof BasicDBList) {
				JsonArray newValue = dbListToJsonArray((BasicDBList)oValue);
				jsonArray.add(newValue);
			} else if (oValue instanceof BasicDBObject) {
				JsonClass newValue = DBObjectToJson((BasicDBObject) oValue);
				jsonArray.add(newValue);
			}
		}
		return jsonArray;
	}
}
