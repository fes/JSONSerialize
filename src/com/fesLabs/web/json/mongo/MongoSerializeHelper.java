/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.fesLabs.web.json.mongo;

import com.fesLabs.web.json.IJsonObjectCreator;
import com.fesLabs.web.json.IJsonSerializeCallback;
import com.fesLabs.web.json.JsonClass;
import com.fesLabs.web.json.JsonSerialize;
import com.fesLabs.web.json.JsonValue;

/**
 *
 * @author fes
 */
public class MongoSerializeHelper implements IJsonSerializeCallback, IJsonObjectCreator {

	private static MongoSerializeHelper instance = null;

	static {
		instance = new MongoSerializeHelper();
	};

	public static void install() {
		JsonSerialize.setObjectCreator(instance);
		JsonSerialize.setSerializeCallback(instance);
	}

	private MongoSerializeHelper() {}

	public void jsonSerializeAdditional(Object o, JsonClass jc) {
		if(o instanceof IJsonAdditional) {
			JsonClass additional = ((IJsonAdditional)o).getJsonAdditional();
			for(String key : additional.getMembers().keySet()) {
				// @TODO: Deep copy?
				jc.add(key, additional.get(key));
			}
		}
	}

	public void jsonHandleUnknownDeserialize(String name, JsonValue value, Object o) {
		if(o instanceof IJsonAdditional) {
			((IJsonAdditional)o).getJsonAdditional().add(name, value);
		}
	}

	public Object jsonCreateObject(Class c) {
		Object o = MongoObjectWrapper.newInstance(c);
		return o;
	}

	public Class jsonClassForObject(Object o) {
		if(o instanceof IJsonAdditional) {
			return ((IJsonAdditional)o).getWrapped();
		} else {
			return o.getClass();
		}
	}
}
