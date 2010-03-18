/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

public class UUIDSerializer implements ICustomSerializer {

  protected static final int nid = 1;
  protected static final int cid = 20;

  public static void registerType() {
    JsonClassDef classDef = new JsonClassDef();
    classDef.serializer = new UUIDSerializer();
    classDef.c = java.util.UUID.class;
    classDef.typeId = cid;
    classDef.namespaceId = nid;
    JsonSerialize.registerType(java.util.UUID.class, classDef);
  }

  public Object deserialize(JsonValue fromValue) {
    if(fromValue instanceof JsonClass) {
      JsonClass fromClass = (JsonClass) fromValue;
      JsonValue value = fromClass.getMembers().get("uuid");
      if(value != null) {
        if(value instanceof JsonString) {
          JsonString jsonString = (JsonString) value;
          java.util.UUID result = java.util.UUID.fromString(jsonString.getValue());
          return result;
        }
      }
    } else if(fromValue instanceof JsonString) {
      return java.util.UUID.fromString(((JsonString)fromValue).getValue());
    }
    return null;
  }

  public JsonValue serialize(Object fromObject, boolean webby) {
    // Always serialize in web format
    webby = true;
    if(fromObject instanceof java.util.UUID) {
      java.util.UUID uuid = (java.util.UUID) fromObject;
      if(!webby) {
        JsonClass jsonClass = new JsonClass();
        jsonClass.add("_cid", new JsonNumber(cid));
        jsonClass.add("_nid", new JsonNumber(nid));
        jsonClass.add("uuid", uuid.toString());
        return jsonClass;
      } else {
        return new JsonString(uuid.toString());
      }
    } else {
      return null;
    }
  }

  public Object createArray(int size) {
    return new java.util.UUID[size];
  }

  public JsonClass getSchema(boolean webby) {
    if(webby) {
      return new JsonClass("{type:\"string\"");
    } else {
      return new JsonClass("{type:\"object\", properties:{uuid:{type:\"string\"}}}");
    }
  }
}
