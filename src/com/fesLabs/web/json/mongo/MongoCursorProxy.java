/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.fesLabs.web.json.mongo;

import com.fesLabs.web.json.JsonClass;
import com.fesLabs.web.json.JsonSerialize;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 *
 * @author fes
 */
public class MongoCursorProxy {
	DBCursor cursor = null;

	public MongoCursorProxy(DBCursor cursor) {
		this.cursor = cursor;
	}

	public boolean hasNext() {
		return this.cursor.hasNext();
	}

	public Object next() {
		JsonClass jc = nextJson();
		if(jc == null) {
			return null;
		}
		return JsonSerialize.deserializeUnknown(jc);
	}

	public JsonClass nextJson() {
		DBObject dbo = this.cursor.next();
		if(dbo == null) {
			return null;
		}
		return MongoConvert.DBObjectToJson(dbo);
	}

	public int count() {
		return this.cursor.count();
	}

	public void skip(int count) {
		this.cursor = this.cursor.skip(count);
	}
}
