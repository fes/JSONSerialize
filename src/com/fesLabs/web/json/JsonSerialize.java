/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import com.fesLabs.web.json.external.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.TreeSet;

public class JsonSerialize {
  public static class JsonTypeDef {
    public Type type = null;
    public JsonTypeDef[] parameterized = null;
    public int typeVal = -1;
    public boolean primitive = false;

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
      if(type instanceof ParameterizedType) {
        this.type = ((ParameterizedType)type).getRawType();
        Type[] parameters = ((ParameterizedType)type).getActualTypeArguments();
        if(parameters != null && parameters.length != 0) {
          this.parameterized = new JsonTypeDef[parameters.length];
          for(int index = 0; index < this.parameterized.length; index++) {
            this.parameterized[index] = new JsonTypeDef(parameters[index]);
          }
        }
      } else {
        this.type = type;
      }
      if(this.type.equals(ArrayList.class)) {
        this.typeVal = SerializeField.TYPE_ARRAYLIST;
      } else if(this.type.equals(TreeSet.class)) {
        this.typeVal = SerializeField.TYPE_TREESET;
      } else if(this.type.equals(TreeMap.class)) {
        this.typeVal = SerializeField.TYPE_TREEMAP;
      } else if(this.type.equals(HashSet.class)) {
        this.typeVal = SerializeField.TYPE_HASHSET;
      } else if(this.type.equals(HashMap.class)) {
        this.typeVal = SerializeField.TYPE_HASHMAP;
      } else if(this.type.equals(String.class)) {
        this.typeVal = SerializeField.TYPE_STRING;
      } else if(this.type.equals(Boolean.class) || this.type.toString().equals("boolean")) {
        this.typeVal = SerializeField.TYPE_BOOLEAN;
      } else if(this.type.equals(Long.class) || this.type.toString().equals("long")) {
        this.typeVal = SerializeField.TYPE_LONG;
      } else if(this.type.equals(Integer.class) || this.type.toString().equals("int")) {
        this.typeVal = SerializeField.TYPE_INT;
      } else if(this.type.equals(Short.class) || this.type.toString().equals("short")) {
        this.typeVal = SerializeField.TYPE_SHORT;
      } else if(this.type.equals(Byte.class) || this.type.toString().equals("byte")) {
        this.typeVal = SerializeField.TYPE_BYTE;
      } else if(this.type.equals(Double.class) || this.type.toString().equals("double")) {
        this.typeVal = SerializeField.TYPE_DOUBLE;
      } else if(this.type.equals(Float.class) || this.type.toString().equals("float")) {
        this.typeVal = SerializeField.TYPE_FLOAT;
      } else if((this.type instanceof Class) && ((Class)this.type).isArray()) {
        String name = ((Class)this.type).getName();
        if(name.startsWith("[B")) {
          this.typeVal = SerializeField.TYPE_BINARY;
        } else {
          this.typeVal = SerializeField.TYPE_ARRAY;
          handleArray(name);
        }
      } else {
        this.typeVal = SerializeField.TYPE_CLASS;
      }
    }

    private void handleArray(String name) {
      while(name.startsWith("[")) {
        name = name.substring(1);
      }
      char c = name.charAt(0);
      int t = 0;
      switch(c) {
        case 'Z':
          t = SerializeField.TYPE_BOOLEAN;
          this.parameterized = new JsonTypeDef[] { new JsonTypeDef(t, true) };
          break;
        case 'B':
          t = SerializeField.TYPE_BYTE;
          this.parameterized = new JsonTypeDef[] { new JsonTypeDef(t, true) };
          break;
        case 'C':
          t = SerializeField.TYPE_CHAR;
          this.parameterized = new JsonTypeDef[] { new JsonTypeDef(t, true) };
          break;
        case 'D':
          t = SerializeField.TYPE_DOUBLE;
          this.parameterized = new JsonTypeDef[] { new JsonTypeDef(t, true) };
          break;
        case 'F':
          t = SerializeField.TYPE_FLOAT;
          this.parameterized = new JsonTypeDef[] { new JsonTypeDef(t, true) };
          break;
        case 'I':
          t = SerializeField.TYPE_INT;
          this.parameterized = new JsonTypeDef[] { new JsonTypeDef(t, true) };
          break;
        case 'J':
          t = SerializeField.TYPE_LONG;
          this.parameterized = new JsonTypeDef[] { new JsonTypeDef(t, true) };
          break;
        case 'S':
          t = SerializeField.TYPE_SHORT;
          this.parameterized = new JsonTypeDef[] { new JsonTypeDef(t, true) };
          break;
        case 'L':
          name = name.substring(1, name.length() - 1);
          if(name.equals("java.lang.Boolean")) {
            t = SerializeField.TYPE_BOOLEAN;
            this.parameterized = new JsonTypeDef[] { new JsonTypeDef(Boolean.class, t) };
          } else if(name.equals("java.lang.Byte")) {
            t = SerializeField.TYPE_BYTE;
            this.parameterized = new JsonTypeDef[] { new JsonTypeDef(Byte.class, t) };
          } else if(name.equals("java.lang.Character")) {
            t = SerializeField.TYPE_CHAR;
            this.parameterized = new JsonTypeDef[] { new JsonTypeDef(Character.class, t) };
          } else if(name.equals("java.lang.Double")) {
            t = SerializeField.TYPE_DOUBLE;
            this.parameterized = new JsonTypeDef[] { new JsonTypeDef(Double.class, t) };
          } else if(name.equals("java.lang.Float")) {
            t = SerializeField.TYPE_FLOAT;
            this.parameterized = new JsonTypeDef[] { new JsonTypeDef(Float.class, t) };
          } else if(name.equals("java.lang.Integer")) {
            t = SerializeField.TYPE_INT;
            this.parameterized = new JsonTypeDef[] { new JsonTypeDef(Integer.class, t) };
          } else if(name.equals("java.lang.Long")) {
            t = SerializeField.TYPE_LONG;
            this.parameterized = new JsonTypeDef[] { new JsonTypeDef(Long.class, t) };
          } else if(name.equals("java.lang.Short")) {
            t = SerializeField.TYPE_SHORT;
            this.parameterized = new JsonTypeDef[] { new JsonTypeDef(Short.class, t) };
          } else if(name.equals("java.lang.String")) {
            t = SerializeField.TYPE_STRING;
            this.parameterized = new JsonTypeDef[] { new JsonTypeDef(String.class, t) };
            //} else if(name.equals("java.util.UUID")) {
            //  t = SerializeField.TYPE_UUID;
          } else {
            try {
              Class ct = Class.forName(name);
              this.parameterized = new JsonTypeDef[] { new JsonTypeDef(ct) };
            } catch(Exception e) {
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
      if(this.parameterized != null) {
        int index = 0;
        sb.append("<");
        for(index = 0; index < this.parameterized.length; index++) {
          if(index != 0) {
            sb.append(",");
          }
          sb.append(this.parameterized[index].toString(indent));
        }
        sb.append(">");
      }
      return sb.toString();
    }
  }

  public static class JsonFieldDef {
    public Field field = null;
    public String name = null;
    public int fieldId = -1;
    public int type = -1;
    public JsonTypeDef typeDef = null;

    public JsonFieldDef(Field field, String name, int fieldId, int type) {
      this.field = field;
      this.name = name;
      this.fieldId = fieldId;
      this.type = type;
      this.typeDef = new JsonTypeDef(field);
    }

    @Override
    public String toString() {
      return toString("");
    }

    public String toString(String indent) {
      StringBuilder sb = new StringBuilder();
      sb.append(indent);
      sb.append(name);
      sb.append(": ");
      sb.append(this.typeDef.toString(indent + "  "));
      return sb.toString();
    }
  }

  public static class JsonClassDef {
    public ICustomSerializer serializer = null;
    public Class c = null;
    public String name = null;
    public int typeId = -1;
    public int namespaceId = -1;
    public TreeMap<Integer, JsonFieldDef> fields = new TreeMap<Integer, JsonFieldDef>();
    public HashMap<String, JsonFieldDef> fieldsByName = new HashMap<String, JsonFieldDef>();

    public JsonClassDef() {
    }

    public JsonClassDef(Class c) {
      this.c = c;
      this.name = c.getSimpleName();
      SerializeClass sc = (SerializeClass) c.getAnnotation(SerializeClass.class);
      if(sc != null) {
        typeId = sc.cid();
        namespaceId = sc.nid();
      }

      do {
        Field[] fields = c.getDeclaredFields();
        for(Field field : fields) {
          int mods = field.getModifiers();
          if((mods & Modifier.STATIC) == 0) {
            SerializeField sf = field.getAnnotation(SerializeField.class);
            if(sf != null) {
              String fieldName = field.getName();
              int fieldType = 0;
              if(String.class.isAssignableFrom(field.getType())) {
                fieldType = SerializeField.TYPE_STRING;
              } else if(HashMap.class.isAssignableFrom(field.getType())) {
                fieldType = SerializeField.TYPE_HASHMAP;
              } else if(TreeMap.class.isAssignableFrom(field.getType())) {
                fieldType = SerializeField.TYPE_TREEMAP;
              } else if(HashSet.class.isAssignableFrom(field.getType())) {
                fieldType = SerializeField.TYPE_HASHSET;
              } else if(TreeSet.class.isAssignableFrom(field.getType())) {
                fieldType = SerializeField.TYPE_TREESET;
              } else if(field.getType().equals(Long.class) || field.getType().toString().equals("long")) {
                fieldType = SerializeField.TYPE_LONG;
              } else if(field.getType().equals(Integer.class) || field.getType().toString().equals("int")) {
                fieldType = SerializeField.TYPE_INT;
              } else if(field.getType().equals(Short.class) || field.getType().toString().equals("short")) {
                fieldType = SerializeField.TYPE_SHORT;
              } else if(field.getType().equals(Byte.class) || field.getType().toString().equals("byte")) {
                fieldType = SerializeField.TYPE_BYTE;
              } else if(field.getType().equals(Double.class) || field.getType().toString().equals("double")) {
                fieldType = SerializeField.TYPE_DOUBLE;
              } else if(field.getType().equals(Float.class) || field.getType().toString().equals("float")) {
                fieldType = SerializeField.TYPE_FLOAT;
              } else if(field.getType().equals(Boolean.class) || field.getType().toString().equals("boolean")) {
                fieldType = SerializeField.TYPE_BOOLEAN;
              } else if(ArrayList.class.isAssignableFrom(field.getType())) {
                fieldType = SerializeField.TYPE_ARRAYLIST;
              } else if(field.getType().isArray()) {
                if(field.getType().getName().startsWith("[B")) {
                  fieldType = SerializeField.TYPE_BINARY;
                } else {
                  fieldType = SerializeField.TYPE_ARRAY;
                }
              } else {
                fieldType = SerializeField.TYPE_CLASS;
              }
              if(fieldType != 0) {
                JsonFieldDef fieldDef = new JsonFieldDef(field, fieldName, sf.value(), fieldType);
                this.fields.put(new Integer(sf.value()), fieldDef);
                this.fieldsByName.put(fieldName, fieldDef);
              }
            }
          }
        }
        c = c.getSuperclass();
      } while(c != null);

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
      for(String name : this.fieldsByName.keySet()) {
        sb.append(this.fieldsByName.get(name).toString(indent + "  "));
        sb.append("\n");
      }
      return sb.toString();
    }
  }

  private static TreeMap<Integer, TreeMap<Integer, JsonClassDef>> registeredTypes = new TreeMap<Integer, TreeMap<Integer, JsonClassDef>>();
  private static HashMap<Class, JsonClassDef> registeredTypesByClass = new HashMap<Class, JsonClassDef>();

  public static void registerType(Class c) {
    JsonClassDef classDef = new JsonClassDef(c);
    registerType(c, classDef);
  }

  public static void registerType(Class c, JsonClassDef classDef) {
    if(classDef.typeId != -1) {
      synchronized(registeredTypes) {
        TreeMap<Integer, JsonClassDef> namespace = null;
        if((namespace = registeredTypes.get(classDef.namespaceId)) == null) {
          namespace = new TreeMap<Integer, JsonClassDef>();
          registeredTypes.put(new Integer(classDef.namespaceId), namespace);
        }
        if(!namespace.containsKey(classDef.typeId)) {
          namespace.put(new Integer(classDef.typeId), classDef);
          registeredTypesByClass.put(c, classDef);
        } else {
          JsonClassDef existing = namespace.get(classDef.typeId);
          if(!c.equals(existing.c)) {
            System.err.println("Class " + c.getName() + " conflicts with existing class: " + existing.c.getName());
          }
        }
      }
    }
  }

  static {
    try {
      InetAddressSerializer.registerType();
      DateSerializer.registerType();
      UUIDSerializer.registerType();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  public static JsonClassDef getType(Class c) {
    return registeredTypesByClass.get(c);
  }

  public static JsonClassDef getType(Integer nid, Integer cid) {
    TreeMap<Integer, JsonClassDef> namespace = null;
    if((namespace = registeredTypes.get(nid)) != null) {
      return namespace.get(cid);
    }
    return null;
  }

  public static JsonClassDef getType(JsonClass jsonClass) {
    JsonValue nid = null;
    JsonValue cid = null;
    cid = jsonClass.getMembers().get("_cid");
    nid = jsonClass.getMembers().get("_nid");
    if(nid != null && cid != null && nid instanceof JsonNumber && cid instanceof JsonNumber) {
      return getType((int)((JsonNumber)nid).getlValue(), (int)((JsonNumber)cid).getlValue());
    }
    return null;
  }

  private static String nameDelimiter = "";
  public static void setQuotes(boolean quotes) {
    if(quotes) {
      nameDelimiter = "\"";
    } else {
      nameDelimiter = "";
    }
  }


  ///////////////////////////////////////////////////////////////////
  // Serialize
  ///////////////////////////////////////////////////////////////////
  public static byte[] serializeJsonToArray(JsonValue o) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
      OutputStreamWriter writer = new OutputStreamWriter(bos, "UTF-8");
      writer.write(o.toString());
      writer.flush();
		} catch(Exception e) {
			return null;
		}
    return bos.toByteArray();
  }

  public static HashSet<String> standardSkipFields = new HashSet<String>();
  static {
    standardSkipFields.add("_cid");
    standardSkipFields.add("_nid");
  }

  public static void serializeJsonToWriter(JsonValue o, Writer writer, boolean webby) {
		try {
      o.serializeTo(writer, standardSkipFields);
      writer.flush();
		} catch(Exception e) {
		}
  }

  public static byte[] serializeUnknownToArray(Object o) {
    return serializeUnknownToArray(o, false);
  }

  public static byte[] serializeUnknownToArray(Object o, boolean webby) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
      OutputStreamWriter writer = new OutputStreamWriter(bos, "UTF-8");
      JsonSerialize.serializeUnknown(o, writer, webby);
      writer.flush();
		} catch(Exception e) {
			return null;
		}
    return bos.toByteArray();
  }

  public static void serializeUnknown(Object o, Writer writer) throws IOException {
    serializeUnknown(o, writer, false);
  }

  public static void serializeUnknown(Object o, Writer writer, boolean webby) throws IOException {
    JsonValue value = serializeUnknown(o, webby);
    value.serializeTo(writer);
  }

  public static JsonValue serializeUnknown(Object o) {
    return serializeUnknown(o, false);
  }

  public static JsonValue serializeUnknown(Object o, boolean webby) {
    JsonClassDef classDef = registeredTypesByClass.get(o.getClass());
    if(classDef == null) {
      if(o instanceof String) {
        return new JsonString((String)o);
      } else if(o instanceof Integer) {
        return new JsonNumber((Integer)o);
      } else if(o instanceof Long) {
        return new JsonNumber((Long)o);
      } else if(o instanceof Short) {
        return new JsonNumber((Short)o);
      } else if(o instanceof Byte) {
        return new JsonNumber(((int) ((Byte)o).byteValue()) & 0x000000ff);
      } else if(o instanceof Float) {
        return new JsonNumber((Float)o);
      } else if(o instanceof Double) {
        return new JsonNumber((Double)o);
      } else if(o instanceof Boolean) {
        return new JsonBoolean((Boolean)o);
        // @TODO: array
      } else if(o instanceof ArrayList) {
        return serializeUnknownList((List)o, webby);
      } else if(o instanceof TreeSet) {
        return serializeUnknownSet((Set)o, webby);
      } else if(o instanceof TreeMap) {
        return serializeUnknownMap((Map)o, webby);
      } else if(o instanceof HashSet) {
        return serializeUnknownSet((Set)o, webby);
      } else if((o instanceof HashMap)
            || (o instanceof ConcurrentHashMap)){

        return serializeUnknownMap((Map)o, webby);
      }
    } else {
      if(classDef.serializer == null) {
        JsonClass result = new JsonClass();
        if(!webby) {
          result.getMembers().put("_cid", new JsonNumber(classDef.typeId));
          result.getMembers().put("_nid", new JsonNumber(classDef.namespaceId));
        }
        for(JsonFieldDef fieldDef : classDef.fieldsByName.values()) {
          try {
            switch(fieldDef.type) {
              case SerializeField.TYPE_BYTE: {
                result.getMembers().put(fieldDef.name, new JsonNumber(fieldDef.field.getByte(o)));
              }
              break;
              case SerializeField.TYPE_SHORT: {
                result.getMembers().put(fieldDef.name, new JsonNumber(fieldDef.field.getShort(o)));
              }
              break;
              case SerializeField.TYPE_INT: {
                result.getMembers().put(fieldDef.name, new JsonNumber(fieldDef.field.getInt(o)));
              }
              break;
              case SerializeField.TYPE_LONG: {
                result.getMembers().put(fieldDef.name, new JsonNumber(fieldDef.field.getLong(o)));
              }
              break;
              case SerializeField.TYPE_FLOAT: {
                result.getMembers().put(fieldDef.name, new JsonNumber(fieldDef.field.getFloat(o)));
              }
              break;
              case SerializeField.TYPE_DOUBLE: {
                result.getMembers().put(fieldDef.name, new JsonNumber(fieldDef.field.getDouble(o)));
              }
              break;
              case SerializeField.TYPE_BOOLEAN: {
                result.getMembers().put(fieldDef.name, new JsonBoolean(fieldDef.field.getBoolean(o)));
              }
              break;
              case SerializeField.TYPE_STRING: {
                result.getMembers().put(fieldDef.name, new JsonString((String)fieldDef.field.get(o)));
              }
              break;
              case SerializeField.TYPE_BINARY: {
                Object val = fieldDef.field.get(o);
                if(val != null) {
                  result.getMembers().put(fieldDef.name, new JsonString(new String(Base64.encode((byte[])val))));
                }
              }
              break;
              case SerializeField.TYPE_ARRAY: {
                Object val = fieldDef.field.get(o);
                if(val != null) {
                  int length = java.lang.reflect.Array.getLength(val);
                  JsonArray jsonArray = new JsonArray();
                  result.getMembers().put(fieldDef.name, jsonArray);
                  for(int index = 0; index < length; index++) {
                    Object aVal = java.lang.reflect.Array.get(val, index);
                    jsonArray.getMembers().add(serializeUnknown(aVal, webby));
                  }
                }
              }
              break;
              case SerializeField.TYPE_ARRAYLIST: {
                ArrayList val = (ArrayList) fieldDef.field.get(o);
                if(val != null) {
                  result.getMembers().put(fieldDef.name, serializeUnknownList(val, webby));
                }
              }
              break;
              case SerializeField.TYPE_TREESET: {
                TreeSet val = (TreeSet) fieldDef.field.get(o);
                if(val != null) {
                  result.getMembers().put(fieldDef.name, serializeUnknownSet(val, webby));
                }
              }
              break;
              case SerializeField.TYPE_TREEMAP: {
                TreeMap val = (TreeMap) fieldDef.field.get(o);
                if(val != null) {
                  result.getMembers().put(fieldDef.name, serializeUnknownMap(val, webby));
                }
              }
              break;
              case SerializeField.TYPE_HASHSET: {
                HashSet val = (HashSet) fieldDef.field.get(o);
                if(val != null) {
                  result.getMembers().put(fieldDef.name, serializeUnknownSet(val, webby));
                }
              }
              break;
              case SerializeField.TYPE_HASHMAP: {
                HashMap val = (HashMap) fieldDef.field.get(o);
                if(val != null) {
                  result.getMembers().put(fieldDef.name, serializeUnknownMap(val, webby));
                }
              }
              break;
              case SerializeField.TYPE_CLASS: {
                Object val = fieldDef.field.get(o);
                if(val != null) {
                  result.getMembers().put(fieldDef.name, serializeUnknown(val, webby));
                }
              }
              break;
            }
          } catch(Exception e) {
            e.printStackTrace();
          }
        }
        return result;
      } else {
        return classDef.serializer.serialize(o, webby);
      }
    }
    return null;
  }

  private static JsonArray serializeUnknownList(List val, boolean webby) {
    if(val != null) {
      JsonArray jsonArray = new JsonArray();
      for(Object aVal : val) {
        jsonArray.getMembers().add(serializeUnknown(aVal, webby));
      }
      return jsonArray;
    }
    return null;
  }

  private static JsonArray serializeUnknownSet(Set val, boolean webby) {
    if(val != null) {
      JsonArray jsonArray = new JsonArray();
      for(Object aVal : val) {
        jsonArray.getMembers().add(serializeUnknown(aVal, webby));
      }
      return jsonArray;
    }
    return null;
  }

  private static JsonClass serializeUnknownMap(Map val, boolean webby) {
    if(val != null) {
      JsonClass jsonClass = new JsonClass();
      if(webby) {
        for(Object aVal : val.keySet()) {
          jsonClass.getMembers().put(aVal.toString(), serializeUnknown(val.get(aVal), webby));
        }
      } else {
        JsonArray jsonArray = new JsonArray();
        jsonClass.getMembers().put("-map", new JsonString(((val instanceof TreeMap) ? "t" : "h")));
        jsonClass.getMembers().put("-values", jsonArray);
        for(Object aVal : val.keySet()) {
          JsonClass arrayClass = new JsonClass();
          jsonArray.getMembers().add(arrayClass);
          arrayClass.getMembers().put("k", serializeUnknown(aVal, webby));
          arrayClass.getMembers().put("v", serializeUnknown(val.get(aVal), webby));
        }
      }
      return jsonClass;
    }
    return null;
  }

  ///////////////////////////////////////////////////////////////////
  // Deserialize
  ///////////////////////////////////////////////////////////////////
  public static JsonValue deserializeJson(byte[] o) {
    if(o == null) {
      return null;
    }
		try {
      String s = new String(o, "UTF-8");
      if(s.startsWith("{")) {
        return JsonClass.fromString(s);
      }
      if(s.startsWith("[")) {
        return JsonArray.fromString(s);
      }
		} catch(Exception e) {
		}
		return null;
  }

  public static Object deserializeUnknown(byte[] bJson) {
    try {
      String strJson = new String(bJson, "UTF-8");
      return deserializeUnknown(strJson);
    } catch(Exception e) {
      return null;
    }
  }

  public static Object deserializeUnknown(String strJson) {
    JsonClass jsonClass = new JsonClass();
    jsonClass.parse(strJson, 0);

    return deserializeUnknown(jsonClass);
  }

  public static Object deserializeUnknown(JsonClass jsonClass) {
    JsonClassDef classDef = getType(jsonClass);
    if(classDef != null) {
      return deserializeUnknown(jsonClass, classDef);
    } else {
      return null;
    }
  }

  public static Object deserializeUnknown(JsonClass jsonClass, JsonClassDef classDef) {
    Object result = null;
    if(classDef != null) {
      if(classDef.serializer != null) {
        return classDef.serializer.deserialize(jsonClass);
      }
      try {
        result = classDef.c.newInstance();
      } catch(Exception e) {
        e.printStackTrace();
        return null;
      }
      for(String name : jsonClass.getMembers().keySet()) {
        try {
          JsonValue jsonValue = jsonClass.getMembers().get(name);
          JsonFieldDef fieldDef = classDef.fieldsByName.get(name);
          if(fieldDef != null) {
            switch(fieldDef.typeDef.typeVal) {
              case SerializeField.TYPE_LONG:
                fieldDef.field.setLong(result, ((JsonNumber)jsonValue).getlValue());
                break;
              case SerializeField.TYPE_INT:
                fieldDef.field.setInt(result, (int)((JsonNumber)jsonValue).getlValue());
                break;
              case SerializeField.TYPE_SHORT:
                fieldDef.field.setShort(result, (short)((JsonNumber)jsonValue).getlValue());
                break;
              case SerializeField.TYPE_BYTE:
                fieldDef.field.setByte(result, (byte)((JsonNumber)jsonValue).getlValue());
                break;
              case SerializeField.TYPE_DOUBLE:
                fieldDef.field.setDouble(result, (double)((JsonNumber)jsonValue).getdValue());
                break;
              case SerializeField.TYPE_FLOAT:
                fieldDef.field.setFloat(result, (float)((JsonNumber)jsonValue).getdValue());
                break;
              case SerializeField.TYPE_BOOLEAN:
                fieldDef.field.setBoolean(result, (boolean)((JsonBoolean)jsonValue).getValue());
                break;
              case SerializeField.TYPE_STRING:
                fieldDef.field.set(result, (String)((JsonString)jsonValue).getValue());
                break;
              case SerializeField.TYPE_BINARY:
                fieldDef.field.set(result, Base64.decode(((JsonString)jsonValue).getValue()));
                break;
              case SerializeField.TYPE_ARRAYLIST:
                fieldDef.field.set(result, deserializeUnknown(jsonValue, fieldDef.typeDef));
                break;
              case SerializeField.TYPE_TREESET:
                fieldDef.field.set(result, deserializeUnknown(jsonValue, fieldDef.typeDef));
                break;
              case SerializeField.TYPE_HASHSET:
                fieldDef.field.set(result, deserializeUnknown(jsonValue, fieldDef.typeDef));
                break;
              case SerializeField.TYPE_TREEMAP:
                fieldDef.field.set(result, deserializeUnknown(jsonValue, fieldDef.typeDef));
                break;
              case SerializeField.TYPE_HASHMAP:
                fieldDef.field.set(result, deserializeUnknown(jsonValue, fieldDef.typeDef));
                break;
              case SerializeField.TYPE_CLASS:
                fieldDef.field.set(result, deserializeUnknown(jsonValue, fieldDef.typeDef));
                break;
              case SerializeField.TYPE_ARRAY:
                fieldDef.field.set(result, deserializeUnknown(jsonValue, fieldDef.typeDef));
                break;
            }
          }
        } catch(Exception e) {
          e.printStackTrace();
        }
      }
    }
    return result;
  }

  public static Object deserializeUnknown(JsonValue jsonValue, JsonTypeDef typeDef) {
    if(typeDef != null) {
      try {
        switch(typeDef.typeVal) {
          case SerializeField.TYPE_LONG:
            return ((JsonNumber)jsonValue).getlValue();
          case SerializeField.TYPE_INT:
            return (int)((JsonNumber)jsonValue).getlValue();
          case SerializeField.TYPE_SHORT:
            return (short)((JsonNumber)jsonValue).getlValue();
          case SerializeField.TYPE_BYTE:
            return (byte)((JsonNumber)jsonValue).getlValue();
          case SerializeField.TYPE_DOUBLE:
            return (double)((JsonNumber)jsonValue).getdValue();
          case SerializeField.TYPE_FLOAT:
            return (float)((JsonNumber)jsonValue).getdValue();
          case SerializeField.TYPE_BOOLEAN:
            return (boolean)((JsonBoolean)jsonValue).getValue();
          case SerializeField.TYPE_STRING:
            return (String)((JsonString)jsonValue).getValue();
          case SerializeField.TYPE_BINARY:
            return Base64.decode(((JsonString)jsonValue).getValue());
          case SerializeField.TYPE_ARRAYLIST: {
              ArrayList arrayList = new ArrayList();
              JsonArray jsonArray = (JsonArray) jsonValue;
              for(JsonValue subValue : jsonArray.getMembers()) {
                Object subObject = null;
                if(typeDef.parameterized != null && typeDef.parameterized.length == 1) {
                  subObject = deserializeUnknown(subValue, typeDef.parameterized[0]);
                } else if(subValue instanceof JsonClass) {
                  subObject = deserializeUnknown((JsonClass)subValue);
                }
                if(subObject != null) {
                  arrayList.add(subObject);
                }
              }
              return arrayList;
            }
          case SerializeField.TYPE_TREESET: {
              TreeSet set = new TreeSet();
              JsonArray jsonArray = (JsonArray) jsonValue;
              for(JsonValue subValue : jsonArray.getMembers()) {
                Object subObject = null;
                if(typeDef.parameterized != null && typeDef.parameterized.length == 1) {
                  subObject = deserializeUnknown(subValue, typeDef.parameterized[0]);
                } else if(subValue instanceof JsonClass) {
                  subObject = deserializeUnknown((JsonClass)subValue);
                }
                if(subObject != null) {
                  set.add(subObject);
                }
              }
              return set;
            }
          case SerializeField.TYPE_HASHSET: {
              HashSet set = new HashSet();
              JsonArray jsonArray = (JsonArray) jsonValue;
              for(JsonValue subValue : jsonArray.getMembers()) {
                Object subObject = null;
                if(typeDef.parameterized != null && typeDef.parameterized.length == 1) {
                  subObject = deserializeUnknown(subValue, typeDef.parameterized[0]);
                } else if(subValue instanceof JsonClass) {
                  subObject = deserializeUnknown((JsonClass)subValue);
                }
                if(subObject != null) {
                  set.add(subObject);
                }
              }
              return set;
            }
          case SerializeField.TYPE_TREEMAP:
          case SerializeField.TYPE_HASHMAP: {
              Map map = null;
              if(typeDef.typeVal == SerializeField.TYPE_TREEMAP) {
                map = new TreeMap();
              } else {
                map = new HashMap();
              }
              JsonClass jsonClass = (JsonClass) jsonValue;
              JsonValue id;
              if((id = jsonClass.getMembers().get("-map")) != null && id instanceof JsonString) {
                id = jsonClass.getMembers().get("-values");
                if(id != null && id instanceof JsonArray) {
                  JsonArray jsonArray = (JsonArray) id;
                  for(JsonValue arrayValue : jsonArray.getMembers()) {
                    if(arrayValue instanceof JsonClass) {
                      JsonClass value = (JsonClass) arrayValue;
                      JsonValue keyValue = value.getMembers().get("k");
                      JsonValue valueValue = value.getMembers().get("v");
                      if(typeDef.parameterized != null && typeDef.parameterized.length == 2) {
                        map.put(deserializeUnknown(keyValue, typeDef.parameterized[0]),
                                deserializeUnknown(valueValue, typeDef.parameterized[1]));
                      } else {
                        Object keyObject = null;
                        if(keyValue instanceof JsonClass) {
                          keyObject = deserializeUnknown((JsonClass)keyValue);
                        } else {
                          keyObject = keyValue.getNative();
                        }
                        Object valueObject = null;
                        if(valueValue instanceof JsonClass) {
                          valueObject = deserializeUnknown((JsonClass)valueValue);
                        } else {
                          valueObject = valueValue.getNative();
                        }
                        if(keyObject != null && valueObject != null) {
                          map.put(keyObject,
                                  valueObject);
                        }
                      }
                    }
                  }
                }
              } else {
                for(String key : jsonClass.getMembers().keySet()) {
                  if(typeDef.parameterized != null && typeDef.parameterized.length == 2) {
                    if(typeDef.parameterized[0].typeVal == SerializeField.TYPE_STRING) {
                      map.put(key, deserializeUnknown(jsonClass.getMembers().get(key), typeDef.parameterized[1]));
                    } else if(typeDef.parameterized[0].typeVal == SerializeField.TYPE_LONG) {
                      map.put(Long.parseLong(key), deserializeUnknown(jsonClass.getMembers().get(key), typeDef.parameterized[1]));
                    } else if(typeDef.parameterized[0].typeVal == SerializeField.TYPE_INT) {
                      map.put(Integer.parseInt(key), deserializeUnknown(jsonClass.getMembers().get(key), typeDef.parameterized[1]));
                    } else if(typeDef.parameterized[0].typeVal == SerializeField.TYPE_SHORT) {
                      map.put(Short.parseShort(key), deserializeUnknown(jsonClass.getMembers().get(key), typeDef.parameterized[1]));
                    } else if(typeDef.parameterized[0].typeVal == SerializeField.TYPE_BYTE) {
                      map.put((byte)Integer.parseInt(key), deserializeUnknown(jsonClass.getMembers().get(key), typeDef.parameterized[1]));
                    } else if(typeDef.parameterized[0].typeVal == SerializeField.TYPE_DOUBLE) {
                      map.put(Double.parseDouble(key), deserializeUnknown(jsonClass.getMembers().get(key), typeDef.parameterized[1]));
                    } else if(typeDef.parameterized[0].typeVal == SerializeField.TYPE_FLOAT) {
                      map.put(Float.parseFloat(key), deserializeUnknown(jsonClass.getMembers().get(key), typeDef.parameterized[1]));
                    } else if(typeDef.parameterized[0].typeVal == SerializeField.TYPE_BOOLEAN) {
                      map.put(Boolean.parseBoolean(key), deserializeUnknown(jsonClass.getMembers().get(key), typeDef.parameterized[1]));
                    }
                  } else {
                    JsonValue valueValue = jsonClass.getMembers().get(key);
                    if(valueValue instanceof JsonClass) {
                      map.put(key, deserializeUnknown((JsonClass) valueValue));
                    } else if(valueValue instanceof JsonString) {
                      map.put(key, ((JsonString)valueValue).getValue());
                    } else if(valueValue instanceof JsonBoolean) {
                      map.put(key, ((JsonBoolean)valueValue).getValue());
                    } else if(valueValue instanceof JsonNumber) {
                      if(((JsonNumber)valueValue).isIsDouble()) {
                        map.put(key, new Double(((JsonNumber)valueValue).getdValue()));
                      } else {
                        map.put(key, new Long(((JsonNumber)valueValue).getlValue()));
                      }
                    } else if(valueValue instanceof JsonArray) {
                      // @TODO: ?
                      //map.put(key, ((JsonBoolean)valueValue).getValue());
                    }
                  }
                }
              }
              return map;
            }
          case SerializeField.TYPE_CLASS:
            return deserializeUnknown((JsonClass)jsonValue);
          case SerializeField.TYPE_ARRAY: {
            if(typeDef.parameterized != null && typeDef.parameterized.length == 1) {
              JsonArray jsonArray = (JsonArray) jsonValue;
              Object result = null;

              JsonTypeDef subDef = typeDef.parameterized[0];

              switch(subDef.typeVal) {
                case SerializeField.TYPE_LONG: {
                  if(subDef.primitive) {
                    result = new long[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((long[])result)[index] = ((JsonNumber)jsonArray.getMembers().get(index)).getlValue();
                    }
                  } else {
                    result = new Long[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((Long[])result)[index] = ((JsonNumber)jsonArray.getMembers().get(index)).getlValue();
                    }
                  }
                }
                break;
                case SerializeField.TYPE_INT: {
                  if(subDef.primitive) {
                    result = new int[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((int[])result)[index] = (int)((JsonNumber)jsonArray.getMembers().get(index)).getlValue();
                    }
                  } else {
                    result = new Integer[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((Integer[])result)[index] = (int)((JsonNumber)jsonArray.getMembers().get(index)).getlValue();
                    }
                  }
                }
                break;
                case SerializeField.TYPE_SHORT: {
                  if(subDef.primitive) {
                    result = new short[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((short[])result)[index] = (short)((JsonNumber)jsonArray.getMembers().get(index)).getlValue();
                    }
                  } else {
                    result = new Short[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((Short[])result)[index] = (short)((JsonNumber)jsonArray.getMembers().get(index)).getlValue();
                    }
                  }
                }
                break;
                case SerializeField.TYPE_BYTE: {
                  result = new Byte[jsonArray.getMembers().size()];
                  for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                    ((Byte[])result)[index] = (byte)((JsonNumber)jsonArray.getMembers().get(index)).getlValue();
                  }
                }
                break;
                case SerializeField.TYPE_DOUBLE: {
                  if(subDef.primitive) {
                    result = new double[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((double[])result)[index] = (double)((JsonNumber)jsonArray.getMembers().get(index)).getdValue();
                    }
                  } else {
                    result = new Double[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((Double[])result)[index] = (double)((JsonNumber)jsonArray.getMembers().get(index)).getdValue();
                    }
                  }
                }
                break;
                case SerializeField.TYPE_FLOAT: {
                  if(subDef.primitive) {
                    result = new float[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((float[])result)[index] = (float)((JsonNumber)jsonArray.getMembers().get(index)).getdValue();
                    }
                  } else {
                    result = new Float[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((Float[])result)[index] = (float)((JsonNumber)jsonArray.getMembers().get(index)).getdValue();
                    }
                  }
                }
                break;
                case SerializeField.TYPE_BOOLEAN: {
                  if(subDef.primitive) {
                    result = new boolean[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((boolean[])result)[index] = ((JsonBoolean)jsonArray.getMembers().get(index)).getValue();
                    }
                  } else {
                    result = new Boolean[jsonArray.getMembers().size()];
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      ((Boolean[])result)[index] = ((JsonBoolean)jsonArray.getMembers().get(index)).getValue();
                    }
                  }
                }
                break;
                case SerializeField.TYPE_STRING: {
                  result = new String[jsonArray.getMembers().size()];
                  for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                    ((String[])result)[index] = ((JsonString)jsonArray.getMembers().get(index)).getValue();
                  }
                }
                break;
                case SerializeField.TYPE_TREESET: {
                  result = new TreeSet[jsonArray.getMembers().size()];
                  for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                    ((TreeSet[])result)[index] = (TreeSet) deserializeUnknown(jsonArray.getMembers().get(index), subDef);
                  }
                }
                break;
                case SerializeField.TYPE_TREEMAP: {
                  result = new TreeMap[jsonArray.getMembers().size()];
                  for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                    ((TreeMap[])result)[index] = (TreeMap) deserializeUnknown(jsonArray.getMembers().get(index), subDef);
                  }
                }
                break;
                case SerializeField.TYPE_HASHSET: {
                  result = new HashSet[jsonArray.getMembers().size()];
                  for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                    ((HashSet[])result)[index] = (HashSet) deserializeUnknown(jsonArray.getMembers().get(index), subDef);
                  }
                }
                break;
                case SerializeField.TYPE_HASHMAP: {
                  result = new HashMap[jsonArray.getMembers().size()];
                  for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                    ((HashMap[])result)[index] = (HashMap) deserializeUnknown(jsonArray.getMembers().get(index), subDef);
                  }
                }
                break;
                case SerializeField.TYPE_ARRAYLIST: {
                  result = new ArrayList[jsonArray.getMembers().size()];
                  for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                    ((ArrayList[])result)[index] = (ArrayList) deserializeUnknown(jsonArray.getMembers().get(index), subDef);
                  }
                }
                break;
                case SerializeField.TYPE_CLASS: {
                  JsonClassDef subClassDef = getType((Class)subDef.type);
                  if(subClassDef.serializer == null) {
                    result = java.lang.reflect.Array.newInstance(subClassDef.c, jsonArray.getMembers().size());
                  } else {
                    result = subClassDef.serializer.createArray(jsonArray.getMembers().size());
                  }
                  if(result != null) {
                    for(int index = 0; index < jsonArray.getMembers().size(); index++) {
                      JsonValue arrayValue = jsonArray.getMembers().get(index);
                      Object o = deserializeUnknown(arrayValue, subDef);
                      java.lang.reflect.Array.set(result, index, o);
                    }
                  }
                }
                break;
              }
              return result;
            }
          }
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
