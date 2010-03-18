/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.fesLabs.web.json.mongo;

import com.fesLabs.web.json.JsonClass;
import com.fesLabs.web.json.JsonSerialize;
import com.mongodb.*;

/**
 *
 * @author fes
 */
public class MongoCollectionProxy {
	DBCollection collection = null;

	public MongoCollectionProxy(DBCollection collection) {
		this.collection = collection;
	}

	// @TODO: Update
	public Object save(Object input) {
		JsonClass jc = (JsonClass) JsonSerialize.serializeUnknown(input);

		jc = saveJson(jc);

		return JsonSerialize.deserializeUnknown(jc);
	}

	public Object updateOne(JsonClass search, JsonClass updateSet) {
		JsonClass result = updateOneJson(search, updateSet);
		if(result != null) {
			return JsonSerialize.deserializeUnknown(result);
		}
		return null;
	}

	public Object updateAll(JsonClass search, JsonClass updateSet) {
		JsonClass result = updateAllJson(search, updateSet);
		if(result != null) {
			return JsonSerialize.deserializeUnknown(result);
		}
		return null;
	}

	public Object findOne(JsonClass search) {
		return findOne(search, null);
	}

	public Object findOne(JsonClass search, JsonClass filter) {
		JsonClass result = findOneJson(search, filter);
		if(result != null) {
			return JsonSerialize.deserializeUnknown(result);
		}
		return null;
	}

	public JsonClass saveJson(JsonClass input) {
		String strId = input.getString("_id");
		if(strId != null) {
			input.remove("_id");
		}

		DBObject dbo = MongoConvert.jsonClassToDBObject((JsonClass) input);

		if(strId != null) {
			dbo.put("_id", new ObjectId(strId));
		}

		collection.save(dbo);

		if(input instanceof IJsonAdditional) {
			return input;
		}

		input.add("_id", dbo.get("_id").toString());

		return input;
	}

	public void remove(JsonClass search) {
		DBObject dbSearch = MongoConvert.jsonClassToDBObject(search);
		collection.remove(dbSearch);
	}

	public JsonClass updateOneJson(JsonClass search, JsonClass updateSet) {
		DBObject dbSearch = MongoConvert.jsonClassToDBObject(search);
		DBObject dbUpdateSet = MongoConvert.jsonClassToDBObject(updateSet);
		collection.update(dbSearch, dbUpdateSet, false, false);
		//if(result != null) {
		//	return MongoConvert.DBObjectToJson(result);
		//}
		return null;
	}

	public JsonClass updateAllJson(JsonClass search, JsonClass updateSet) {
		DBObject dbSearch = MongoConvert.jsonClassToDBObject(search);
		DBObject dbUpdateSet = MongoConvert.jsonClassToDBObject(updateSet);
		collection.updateMulti(dbSearch, dbUpdateSet);
		//result = collection.update(dbSearch, dbUpdateSet, true, true);
		//if(result != null) {
		//	return MongoConvert.DBObjectToJson(result);
		//}
		return null;
	}

	public JsonClass findOneJson(JsonClass search) {
		return findOneJson(search, null);
	}

	public JsonClass findOneJson(JsonClass search, JsonClass filter) {
		DBObject result = null;
		DBObject dbSearch = MongoConvert.jsonClassToDBObject(search);
		if(filter != null) {
			DBObject dbFilter = MongoConvert.jsonClassToDBObject(filter);
			result = collection.findOne(dbSearch, dbFilter);
		} else {
			result = collection.findOne(dbSearch);
		}
		if(result != null) {
			return MongoConvert.DBObjectToJson(result);
		}
		return null;
	}

	public MongoCursorProxy find(JsonClass search) {
		return find(search, null);
	}

	public MongoCursorProxy find(JsonClass search, JsonClass filter) {
		return find(search, filter, null);
	}

	public MongoCursorProxy find(JsonClass search, JsonClass filter, JsonClass sortOrder) {
		DBCursor result = null;
		DBObject dbSearch = MongoConvert.jsonClassToDBObject(search);
		if(filter != null) {
			DBObject dbFilter = MongoConvert.jsonClassToDBObject(filter);
			result = collection.find(dbSearch, dbFilter);
		} else {
			result = collection.find(dbSearch);
		}
		if(result != null) {
			if(sortOrder != null) {
				result.sort(MongoConvert.jsonClassToDBObject(sortOrder));
			}
			return new MongoCursorProxy(result);
		}
		return null;
	}
}
