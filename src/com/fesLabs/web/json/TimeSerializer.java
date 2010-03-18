/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/
package com.fesLabs.web.json;

public class TimeSerializer implements ICustomSerializer {

  protected static final int nid = 1;
  protected static final int cid = 13;

  public static void registerType() {
    JsonClassDef classDef = new JsonClassDef();
    classDef.serializer = new TimeSerializer();
    classDef.c = java.sql.Time.class;
    classDef.typeId = cid;
    classDef.namespaceId = nid;
    JsonSerialize.registerType(java.sql.Time.class, classDef);
  }

  public Object deserialize(JsonValue fromValue) {
    if (fromValue instanceof JsonClass) {
      JsonClass fromClass = (JsonClass) fromValue;
      JsonValue value = fromClass.getMembers().get("time");
      if (value != null) {
        long lvalue = 0;
        if (value instanceof JsonNumber) {
          JsonNumber jsonNumber = (JsonNumber) value;
          lvalue = jsonNumber.getAsLong();
        } else if (value instanceof JsonString) {
          JsonString jsonString = (JsonString) value;
          lvalue = Long.parseLong(jsonString.getValue());
        }
        return new java.sql.Time(lvalue);
      }
    } else if (fromValue instanceof JsonNumber) {
      return new java.sql.Time(((JsonNumber) fromValue).getAsLong());
    }
    return null;
  }

  public JsonValue serialize(Object fromObject, boolean webby) {
    // Always serialize in web format
    webby = true;
    if (fromObject instanceof java.sql.Time) {
      java.sql.Time time = (java.sql.Time) fromObject;
      if (!webby) {
        JsonClass jsonClass = new JsonClass();
        jsonClass.add("_cid", new JsonNumber(cid));
        jsonClass.add("_nid", new JsonNumber(nid));
        jsonClass.add("time", new JsonNumber(time.getTime()));
        return jsonClass;
      } else {
        return new JsonNumber(time.getTime());
      }
    } else {
      return null;
    }
  }

  public Object createArray(int size) {
    return new java.sql.Time[size];
  }

  public JsonClass getSchema(boolean webby) {
    if(webby) {
      return new JsonClass("{type:\"number\"");
    } else {
      return new JsonClass("{type:\"object\", properties:{time:{type:\"number\"}}}");
    }
  }
}
