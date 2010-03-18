/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaDescription {
  String value();
}
