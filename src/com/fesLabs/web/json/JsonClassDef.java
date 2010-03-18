/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/
package com.fesLabs.web.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class JsonClassDef {

  public ICustomSerializer serializer = null;
  public Class c = null;
  public String name = null;
  public int typeId = -1;
  public int namespaceId = -1;
  public HashMap<String, JsonFieldDef> fieldsByName = new HashMap<String, JsonFieldDef>();
  public String schemaDescription = null;

  public JsonClassDef() {
  }

  public JsonClassDef(Class c) {
    this.c = c;
    this.name = c.getSimpleName();
    SerializeClass sc = (SerializeClass) c.getAnnotation(SerializeClass.class);
    if (sc != null) {
      typeId = sc.cid();
      namespaceId = sc.nid();
    }
    SchemaDescription sd = (SchemaDescription) c.getAnnotation(SchemaDescription.class);
    if (sd != null) {
      this.schemaDescription = sd.value();
    }

    do {
      Field[] fields = c.getDeclaredFields();
      for (Field field : fields) {
        int mods = field.getModifiers();
        if ((mods & Modifier.STATIC) == 0) {
          SerializeField sf = field.getAnnotation(SerializeField.class);
          if (sf != null) {
            String fieldName = field.getName();
            int fieldType = 0;
            if (String.class.isAssignableFrom(field.getType())) {
              fieldType = SerializeField.TYPE_STRING;
            } else if (java.util.regex.Pattern.class.isAssignableFrom(field.getType())) {
              fieldType = SerializeField.TYPE_REGEX;
            } else if (HashMap.class.isAssignableFrom(field.getType())) {
              fieldType = SerializeField.TYPE_HASHMAP;
            } else if (TreeMap.class.isAssignableFrom(field.getType())) {
              fieldType = SerializeField.TYPE_TREEMAP;
            } else if (HashSet.class.isAssignableFrom(field.getType())) {
              fieldType = SerializeField.TYPE_HASHSET;
            } else if (TreeSet.class.isAssignableFrom(field.getType())) {
              fieldType = SerializeField.TYPE_TREESET;
            } else if (field.getType().equals(Long.class) || field.getType().toString().equals("long")) {
              fieldType = SerializeField.TYPE_LONG;
            } else if (field.getType().equals(Integer.class) || field.getType().toString().equals("int")) {
              fieldType = SerializeField.TYPE_INT;
            } else if (field.getType().equals(Short.class) || field.getType().toString().equals("short")) {
              fieldType = SerializeField.TYPE_SHORT;
            } else if (field.getType().equals(Byte.class) || field.getType().toString().equals("byte")) {
              fieldType = SerializeField.TYPE_BYTE;
            } else if (field.getType().equals(Double.class) || field.getType().toString().equals("double")) {
              fieldType = SerializeField.TYPE_DOUBLE;
            } else if (field.getType().equals(Float.class) || field.getType().toString().equals("float")) {
              fieldType = SerializeField.TYPE_FLOAT;
            } else if (field.getType().equals(Boolean.class) || field.getType().toString().equals("boolean")) {
              fieldType = SerializeField.TYPE_BOOLEAN;
            } else if (ArrayList.class.isAssignableFrom(field.getType())) {
              fieldType = SerializeField.TYPE_ARRAYLIST;
            } else if (field.getType().isArray()) {
              if (field.getType().getName().startsWith("[B")) {
                fieldType = SerializeField.TYPE_BINARY;
              } else {
                fieldType = SerializeField.TYPE_ARRAY;
              }
            } else {
              if (JsonSerialize.getType(field.getType()) != null) {
                fieldType = SerializeField.TYPE_CUSTOM;
              } else if (JsonValue.class.isAssignableFrom((Class) field.getType())) {
                fieldType = SerializeField.TYPE_JSON;
              } else {
                fieldType = SerializeField.TYPE_CLASS;
              }
            }
            if (fieldType != 0) {
              JsonFieldDef fieldDef = new JsonFieldDef(field, fieldName, fieldType);
              this.fieldsByName.put(fieldName, fieldDef);
            }
          }
        }
      }
      c = c.getSuperclass();
    } while (c != null);

  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent);
    sb.append(this.name);
    sb.append("\n");
    for (String name : this.fieldsByName.keySet()) {
      sb.append(this.fieldsByName.get(name).toString(indent + "  "));
      sb.append("\n");
    }
    return sb.toString();
  }
}
