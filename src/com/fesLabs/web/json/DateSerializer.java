/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

public class DateSerializer implements ICustomSerializer {
  public static void registerType() {
    JsonSerialize.JsonClassDef classDef = new JsonSerialize.JsonClassDef();
    classDef.serializer = new DateSerializer();
    classDef.c = java.util.Date.class;
    classDef.typeId = 10;
    classDef.namespaceId = 101;
    JsonSerialize.registerType(java.util.Date.class, classDef);

    classDef = new JsonSerialize.JsonClassDef();
    classDef.serializer = new DateSerializer();
    classDef.c = java.sql.Timestamp.class;
    classDef.typeId = 11;
    classDef.namespaceId = 101;
    JsonSerialize.registerType(java.sql.Timestamp.class, classDef);
  }

  public Object deserialize(JsonClass fromClass) {
    JsonValue value = fromClass.getMembers().get("time");
    if(value != null) {
      long lvalue = 0;
      if(value instanceof JsonNumber) {
        JsonNumber jsonNumber = (JsonNumber) value;
        lvalue = jsonNumber.getAsLong();
      } else if(value instanceof JsonString) {
        JsonString jsonString = (JsonString) value;
        lvalue = Long.parseLong(jsonString.getValue());
      }
      if(fromClass.getLong("_cid", 10) == 11) {
        return new java.util.Date(lvalue);
      } else {
        return new java.sql.Timestamp(lvalue);
      }
    }
    return null;
  }

  public JsonClass serialize(Object fromObject, boolean webby) {
    if(fromObject instanceof java.util.Date) {
      java.util.Date date = (java.util.Date) fromObject;
      JsonClass jsonClass = new JsonClass();
      if(!webby) {
        jsonClass.add("_cid", new JsonNumber(((fromObject instanceof java.sql.Timestamp) ? 11 : 10)));
        jsonClass.add("_nid", new JsonNumber(101));
      }
      jsonClass.add("time", new JsonNumber(date.getTime()));
      return jsonClass;
    } else {
      return null;
    }
  }

  public Object createArray(int size) {
    return new java.util.Date[size];
  }
}
