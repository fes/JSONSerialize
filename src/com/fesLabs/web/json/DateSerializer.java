/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/
package com.fesLabs.web.json;

public class DateSerializer implements ICustomSerializer {

  protected static final int nid = 1;
  protected static final int cidDate = 10;
  protected static final int cidTimestamp = 11;
  protected static final int cidSqlDate = 12;

  public static void registerType() {
    JsonClassDef classDef = new JsonClassDef();
    classDef.serializer = new DateSerializer();
    classDef.c = java.util.Date.class;
    classDef.typeId = cidDate;
    classDef.namespaceId = nid;
    JsonSerialize.registerType(java.util.Date.class, classDef);

    classDef = new JsonClassDef();
    classDef.serializer = new DateSerializer();
    classDef.c = java.sql.Timestamp.class;
    classDef.typeId = cidTimestamp;
    classDef.namespaceId = nid;
    JsonSerialize.registerType(java.sql.Timestamp.class, classDef);

    classDef = new JsonClassDef();
    classDef.serializer = new DateSerializer();
    classDef.c = java.sql.Date.class;
    classDef.typeId = cidSqlDate;
    classDef.namespaceId = nid;
    JsonSerialize.registerType(java.sql.Date.class, classDef);
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
        if (fromClass.getLong("_cid", 0) == cidDate) {
          return new java.sql.Timestamp(lvalue);
        } else if (fromClass.getLong("_cid", 0) == cidTimestamp) {
          return new java.util.Date(lvalue);
        } else if (fromClass.getLong("_cid", 0) == cidSqlDate) {
          return new java.sql.Date(lvalue);
        }
      }
    } else if (fromValue instanceof JsonNumber) {
      return new java.util.Date(((JsonNumber) fromValue).getAsLong());
    }
    return null;
  }

  public JsonValue serialize(Object fromObject, boolean webby) {
    // Always serialize in web format
    webby = true;
    if (fromObject instanceof java.util.Date) {
      java.util.Date date = (java.util.Date) fromObject;
      if (!webby) {
        JsonClass jsonClass = new JsonClass();
        jsonClass.add("_cid", new JsonNumber(((fromObject instanceof java.sql.Timestamp) ? cidTimestamp : cidDate)));
        jsonClass.add("_nid", new JsonNumber(nid));
        jsonClass.add("time", new JsonNumber(date.getTime()));
        return jsonClass;
      } else {
        return new JsonNumber(date.getTime());
      }
    } else if (fromObject instanceof java.sql.Date) {
      java.sql.Date date = (java.sql.Date) fromObject;
      if (!webby) {
        JsonClass jsonClass = new JsonClass();
        jsonClass.add("_cid", new JsonNumber(cidSqlDate));
        jsonClass.add("_nid", new JsonNumber(nid));
        jsonClass.add("time", new JsonNumber(date.getTime()));
        return jsonClass;
      } else {
        return new JsonNumber(date.getTime());
      }
    } else {
      return null;
    }
  }

  public Object createArray(int size) {
    return new java.util.Date[size];
  }

  public JsonClass getSchema(boolean webby) {
    if(webby) {
      return new JsonClass("{type:\"number\"");
    } else {
      return new JsonClass("{type:\"object\", properties:{time:{type:\"number\"}}}");
    }
  }
}
