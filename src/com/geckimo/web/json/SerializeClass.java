/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 Geckimo --*/

package com.geckimo.monitor.json;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SerializeClass {
  int cid();
  int nid();
}
