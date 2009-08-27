/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 Geckimo --*/

package com.geckimo.monitor.json;

public interface ICustomSerializer {
  public Object deserialize(JsonClass fromClass);
  public JsonClass serialize(Object fromObject, boolean webby);
  public Object createArray(int size);
}
