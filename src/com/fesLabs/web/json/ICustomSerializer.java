/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

public interface ICustomSerializer {
  public Object deserialize(JsonClass fromClass);
  public JsonClass serialize(Object fromObject, boolean webby);
  public Object createArray(int size);
}
