/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/
package com.fesLabs.web.json;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class JsonTypeDef {

  public Type type = null;
  public JsonTypeDef[] parameterized = null;
  public int typeVal = -1;
  public boolean primitive = false;
  public JsonClassDef custom = null;

  public JsonTypeDef(int typeVal) {
    this.typeVal = typeVal;
  }

  public JsonTypeDef(int typeVal, boolean primitive) {
    this.typeVal = typeVal;
    this.primitive = primitive;
  }

  public JsonTypeDef(Type type, int typeVal) {
    this.type = type;
    this.typeVal = typeVal;
  }

  public JsonTypeDef(Type type, int typeVal, boolean primitive) {
    this.type = type;
    this.typeVal = typeVal;
    this.primitive = primitive;
  }

  public JsonTypeDef(Type type) {
    fromType(type);
  }

  public JsonTypeDef(Field field) {
    Type type = field.getGenericType();
    fromType(type);
  }

  private void fromType(Type type) {
    if (type instanceof Class
            && ((this.custom = JsonSerialize.getType((Class)type)) != null)
            && ((this.custom = JsonSerialize.getType((Class)type)).serializer != null)) {
      this.type = type;
      this.typeVal = SerializeField.TYPE_CUSTOM;
      return;
    }
    if (type instanceof ParameterizedType) {
      this.type = ((ParameterizedType) type).getRawType();
      Type[] parameters = ((ParameterizedType) type).getActualTypeArguments();
      if (parameters != null && parameters.length != 0) {
        this.parameterized = new JsonTypeDef[parameters.length];
        for (int index = 0; index < this.parameterized.length; index++) {
          this.parameterized[index] = new JsonTypeDef(parameters[index]);
        }
      }
    } else {
      this.type = type;
    }
    if (this.type.equals(ArrayList.class)) {
      this.typeVal = SerializeField.TYPE_ARRAYLIST;
    } else if (this.type.equals(TreeSet.class)) {
      this.typeVal = SerializeField.TYPE_TREESET;
    } else if (this.type.equals(TreeMap.class)) {
      this.typeVal = SerializeField.TYPE_TREEMAP;
    } else if (this.type.equals(HashSet.class)) {
      this.typeVal = SerializeField.TYPE_HASHSET;
    } else if (this.type.equals(HashMap.class)) {
      this.typeVal = SerializeField.TYPE_HASHMAP;
    } else if (this.type.equals(java.util.regex.Pattern.class)) {
      this.typeVal = SerializeField.TYPE_REGEX;
    } else if (this.type.equals(String.class)) {
      this.typeVal = SerializeField.TYPE_STRING;
    } else if (this.type.equals(Boolean.class) || this.type.toString().equals("boolean")) {
      this.typeVal = SerializeField.TYPE_BOOLEAN;
    } else if (this.type.equals(Long.class) || this.type.toString().equals("long")) {
      this.typeVal = SerializeField.TYPE_LONG;
    } else if (this.type.equals(Integer.class) || this.type.toString().equals("int")) {
      this.typeVal = SerializeField.TYPE_INT;
    } else if (this.type.equals(Short.class) || this.type.toString().equals("short")) {
      this.typeVal = SerializeField.TYPE_SHORT;
    } else if (this.type.equals(Byte.class) || this.type.toString().equals("byte")) {
      this.typeVal = SerializeField.TYPE_BYTE;
    } else if (this.type.equals(Double.class) || this.type.toString().equals("double")) {
      this.typeVal = SerializeField.TYPE_DOUBLE;
    } else if (this.type.equals(Float.class) || this.type.toString().equals("float")) {
      this.typeVal = SerializeField.TYPE_FLOAT;
    } else if ((this.type instanceof Class) && ((Class) this.type).isArray()) {
      String name = ((Class) this.type).getName();
      if (name.startsWith("[B")) {
        this.typeVal = SerializeField.TYPE_BINARY;
      } else {
        this.typeVal = SerializeField.TYPE_ARRAY;
        handleArray(name);
      }
    } else {
      if (JsonValue.class.isAssignableFrom((Class) this.type)) {
        this.typeVal = SerializeField.TYPE_JSON;
      } else {
        this.typeVal = SerializeField.TYPE_CLASS;
      }
    }
  }

  private void handleArray(String name) {
    while (name.startsWith("[")) {
      name = name.substring(1);
    }
    char c = name.charAt(0);
    int t = 0;
    switch (c) {
      case 'Z':
        t = SerializeField.TYPE_BOOLEAN;
        this.parameterized = new JsonTypeDef[]{new JsonTypeDef(t, true)};
        break;
      case 'B':
        t = SerializeField.TYPE_BYTE;
        this.parameterized = new JsonTypeDef[]{new JsonTypeDef(t, true)};
        break;
      case 'C':
        t = SerializeField.TYPE_CHAR;
        this.parameterized = new JsonTypeDef[]{new JsonTypeDef(t, true)};
        break;
      case 'D':
        t = SerializeField.TYPE_DOUBLE;
        this.parameterized = new JsonTypeDef[]{new JsonTypeDef(t, true)};
        break;
      case 'F':
        t = SerializeField.TYPE_FLOAT;
        this.parameterized = new JsonTypeDef[]{new JsonTypeDef(t, true)};
        break;
      case 'I':
        t = SerializeField.TYPE_INT;
        this.parameterized = new JsonTypeDef[]{new JsonTypeDef(t, true)};
        break;
      case 'J':
        t = SerializeField.TYPE_LONG;
        this.parameterized = new JsonTypeDef[]{new JsonTypeDef(t, true)};
        break;
      case 'S':
        t = SerializeField.TYPE_SHORT;
        this.parameterized = new JsonTypeDef[]{new JsonTypeDef(t, true)};
        break;
      case 'L':
        name = name.substring(1, name.length() - 1);
        if (name.equals("java.lang.Boolean")) {
          t = SerializeField.TYPE_BOOLEAN;
          this.parameterized = new JsonTypeDef[]{new JsonTypeDef(Boolean.class, t)};
        } else if (name.equals("java.lang.Byte")) {
          t = SerializeField.TYPE_BYTE;
          this.parameterized = new JsonTypeDef[]{new JsonTypeDef(Byte.class, t)};
        } else if (name.equals("java.lang.Character")) {
          t = SerializeField.TYPE_CHAR;
          this.parameterized = new JsonTypeDef[]{new JsonTypeDef(Character.class, t)};
        } else if (name.equals("java.lang.Double")) {
          t = SerializeField.TYPE_DOUBLE;
          this.parameterized = new JsonTypeDef[]{new JsonTypeDef(Double.class, t)};
        } else if (name.equals("java.lang.Float")) {
          t = SerializeField.TYPE_FLOAT;
          this.parameterized = new JsonTypeDef[]{new JsonTypeDef(Float.class, t)};
        } else if (name.equals("java.lang.Integer")) {
          t = SerializeField.TYPE_INT;
          this.parameterized = new JsonTypeDef[]{new JsonTypeDef(Integer.class, t)};
        } else if (name.equals("java.lang.Long")) {
          t = SerializeField.TYPE_LONG;
          this.parameterized = new JsonTypeDef[]{new JsonTypeDef(Long.class, t)};
        } else if (name.equals("java.lang.Short")) {
          t = SerializeField.TYPE_SHORT;
          this.parameterized = new JsonTypeDef[]{new JsonTypeDef(Short.class, t)};
        } else if (name.equals("java.lang.String")) {
          t = SerializeField.TYPE_STRING;
          this.parameterized = new JsonTypeDef[]{new JsonTypeDef(String.class, t)};
          //} else if(name.equals("java.util.UUID")) {
          //  t = SerializeField.TYPE_UUID;
        } else {
          try {
            Class ct = Class.forName(name);
            this.parameterized = new JsonTypeDef[]{new JsonTypeDef(ct)};
          } catch (Exception e) {
          }
        }
        break;
    }
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    StringBuilder sb = new StringBuilder();
    sb.append(this.typeVal);
    if (this.parameterized != null) {
      int index = 0;
      sb.append("<");
      for (index = 0; index < this.parameterized.length; index++) {
        if (index != 0) {
          sb.append(",");
        }
        sb.append(this.parameterized[index].toString(indent));
      }
      sb.append(">");
    }
    return sb.toString();
  }
}