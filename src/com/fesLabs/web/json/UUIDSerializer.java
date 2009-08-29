/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

public class UUIDSerializer implements ICustomSerializer {
  public static void registerType() {
    JsonSerialize.JsonClassDef classDef = new JsonSerialize.JsonClassDef();
    classDef.serializer = new UUIDSerializer();
    classDef.c = java.util.UUID.class;
    classDef.typeId = 20;
    classDef.namespaceId = 101;
    JsonSerialize.registerType(java.util.UUID.class, classDef);
  }

  public Object deserialize(JsonClass fromClass) {
    JsonValue value = fromClass.getMembers().get("uuid");
    if(value != null) {
      if(value instanceof JsonString) {
        JsonString jsonString = (JsonString) value;
        java.util.UUID result = java.util.UUID.fromString(jsonString.getValue());
        return result;
      }
    }
    return null;
  }

  public JsonClass serialize(Object fromObject, boolean webby) {
    if(fromObject instanceof java.util.UUID) {
      java.util.UUID uuid = (java.util.UUID) fromObject;
      JsonClass jsonClass = new JsonClass();
      if(!webby) {
        jsonClass.add("_cid", new JsonNumber(20));
        jsonClass.add("_nid", new JsonNumber(101));
      }
      jsonClass.add("uuid", uuid.toString());
      return jsonClass;
    } else {
      return null;
    }
  }

  public Object createArray(int size) {
    return new java.util.UUID[size];
  }
}
