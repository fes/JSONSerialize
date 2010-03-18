/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

public interface ICustomSerializer {
  public Object deserialize(JsonValue fromValue);
  public JsonValue serialize(Object fromObject, boolean webby);
  public Object createArray(int size);
  public JsonClass getSchema(boolean webby);
}
