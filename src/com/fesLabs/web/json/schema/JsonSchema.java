/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/
package com.fesLabs.web.json.schema;

import com.fesLabs.web.json.JsonArray;
import com.fesLabs.web.json.JsonBoolean;
import com.fesLabs.web.json.JsonClass;
import com.fesLabs.web.json.JsonClassDef;
import com.fesLabs.web.json.JsonFieldDef;
import com.fesLabs.web.json.JsonNumber;
import com.fesLabs.web.json.JsonRegex;
import com.fesLabs.web.json.JsonSerialize;
import com.fesLabs.web.json.JsonString;
import com.fesLabs.web.json.JsonTypeDef;
import com.fesLabs.web.json.JsonValue;
import com.fesLabs.web.json.SerializeField;
import java.io.IOException;
import java.io.Writer;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class JsonSchema {

  public JsonClass generateSchema(Class c, int level, boolean webby) {
    return generateSchema(c, level, webby, false);
  }

  public JsonClass generateSchema(Class c, int level, boolean webby, boolean definedAreRefs) {
    JsonClass schema = new JsonClass("{type:\"object\", properties:{}}");

    JsonClassDef classDef = JsonSerialize.getType(c);
    if (definedAreRefs) {
      return generateReference(c, level, webby);
    } else {
      if (classDef.schemaDescription != null) {
        schema.add("description", classDef.schemaDescription);
      }
      for (String key : classDef.fieldsByName.keySet()) {
        JsonFieldDef field = classDef.fieldsByName.get(key);
        JsonTypeDef type = field.typeDef;
        if (field.forSchemaLevel(level)) {
          if (field.schemaTypes != null) {
            JsonClass fieldDesc = new JsonClass("{type:\"object\", properties:[]}");
            for (Class d : field.schemaTypes) {
              fieldDesc.getArray("properties").add(generateReference(d, level, webby));
            }
            schema.getClass("properties").add(key, fieldDesc);
          } else {
            JsonClass fieldDesc = descFromType(type, level, webby);
            if (fieldDesc != null) {
              if (field.schemaDescription != null) {
                fieldDesc.add("description", field.schemaDescription);
              }
              schema.getClass("properties").add(key, fieldDesc);
            }
          }
        }
      }
    }

    return schema;
  }

  public JsonClass generateReference(Class c, int level, boolean webby) {
    JsonClass schema = new JsonClass().add("$ref", "java://" + c.getCanonicalName() + "/?level=" + level + "&webby=" + webby);
    return schema;
  }

  public JsonClass descFromType(JsonTypeDef type, int level, boolean webby) {
    JsonClass fieldDesc = null;
    switch (type.typeVal) {
      case SerializeField.TYPE_BYTE:
        fieldDesc = new JsonClass("{type:\"integer\",minimum:-128,maximum:127}");
        break;
      case SerializeField.TYPE_SHORT:
        fieldDesc = new JsonClass("{type:\"integer\",minimum:-32768,maximum:32767}");
        break;
      case SerializeField.TYPE_INT:
        fieldDesc = new JsonClass("{type:\"integer\",minimum:-2147483648,maximum:2147483647}");
        break;
      case SerializeField.TYPE_LONG:
        fieldDesc = new JsonClass("{type:\"integer\"}");
        break;
      case SerializeField.TYPE_FLOAT:
        fieldDesc = new JsonClass("{type:\"number\"}");
        break;
      case SerializeField.TYPE_DOUBLE:
        fieldDesc = new JsonClass("{type:\"number\"}");
        break;
      case SerializeField.TYPE_BOOLEAN:
        fieldDesc = new JsonClass("{type:\"boolean\"}");
        break;
      case SerializeField.TYPE_STRING:
        fieldDesc = new JsonClass("{type:\"string\"}");
        break;
      case SerializeField.TYPE_REGEX:
        fieldDesc = new JsonClass("{type:\"regex\"}");
        break;
      case SerializeField.TYPE_BINARY:
        fieldDesc = new JsonClass("{type:\"string\"}");
        break;
      case SerializeField.TYPE_ARRAY:
        fieldDesc = new JsonClass("{type:\"array\"}");
        fieldDesc.add("items", descFromType(type.parameterized[0], level, webby));
        break;
      case SerializeField.TYPE_ARRAYLIST:
        fieldDesc = new JsonClass("{type:\"array\"}");
        fieldDesc.add("items", descFromType(type.parameterized[0], level, webby));
        break;
      case SerializeField.TYPE_TREESET:
      case SerializeField.TYPE_HASHSET:
        fieldDesc = new JsonClass("{type:\"array\"}");
        fieldDesc.add("items", descFromType(type.parameterized[0], level, webby));
        break;
      case SerializeField.TYPE_TREEMAP:
      case SerializeField.TYPE_HASHMAP:
        if (webby) {
          // @FES: introduce "map" type
          fieldDesc = new JsonClass("{type:\"map\"}");
          fieldDesc.add("items", descFromType(type.parameterized[1], level, webby));
        } else {
          fieldDesc = new JsonClass("{type:\"array\"}");
          fieldDesc.add("items", new JsonClass("{type:\"object\",properties:{}}"));
          fieldDesc.getClass("items").getClass("properties").add("k", descFromType(type.parameterized[0], level, webby));
          fieldDesc.getClass("items").getClass("properties").add("v", descFromType(type.parameterized[1], level, webby));
        }
        break;
      case SerializeField.TYPE_CLASS:
        fieldDesc = generateSchema((Class) type.type, level, webby, true);
        break;
      case SerializeField.TYPE_CHAR:
        fieldDesc = new JsonClass("{type:\"string\",minLength:1,maxLength:1}");
        break;
      case SerializeField.TYPE_CUSTOM:
        fieldDesc = type.custom.serializer.getSchema(webby);
        break;
      case SerializeField.TYPE_JSON:
        fieldDesc = new JsonClass("{type:[\"object\",\"array\"],any:true}");
        break;
      // @TODO: Regex
    }
    return fieldDesc;
  }

  public class JsonSchemaException extends Exception {

    public JsonSchemaException(String warning) {
      super(warning);
    }
  }

  public String setToString(Set<String> values) {
    StringBuffer sb = new StringBuffer();
    for (String value : values) {
      if (sb.length() != 0) {
        sb.append(", ");
      }
      sb.append(value);
    }
    return sb.toString();
  }
  protected TreeMap<String, JsonClass> schemas = new TreeMap<String, JsonClass>();

  public JsonClass loadSchema(String url) {
    JsonClass schema = null;
    try {
      if (url.startsWith("java://")) {
        int index = url.indexOf("/", 7);
        String host = url.substring(7, index);
        String query = url.substring(url.indexOf("?", index) + 1);
        boolean webby = true;
        int level = 0;
        String[] parts = query.split("&");
        for (String part : parts) {
          index = part.indexOf("=");
          String key;
          String value = null;
          if (index != -1) {
            key = part.substring(0, index);
            value = part.substring(index + 1);
          } else {
            key = part;
          }
          if (key.equals("level")) {
            level = Integer.parseInt(value);
          } else if (key.equals("webby")) {
            if (value == null || value.equals("true")) {
              webby = true;
            } else {
              webby = false;
            }
          }
        }
        url = "java://" + host + "/?webby=" + webby + "&level=" + level;
        synchronized (schemas) {
          schema = schemas.get(url);
        }
        if (schema == null) {
          Class c = Class.forName(host);
          if (c != null) {
            schema = generateSchema(c, level, webby);
            synchronized (schemas) {
              schemas.put(url, schema);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return schema;
  }

  public void safeVisit(JsonValue instance,
          String schemaUrl,
          boolean recursive,
          boolean removeViolations) {
    try {
      visit(instance, schemaUrl, recursive, removeViolations);
    } catch (JsonSchemaException e) {
    }
  }

  public void visit(JsonValue instance,
          String schemaUrl,
          boolean recursive,
          boolean removeViolations) throws JsonSchemaException {
    JsonClass schema = loadSchema(schemaUrl);
    visit(instance, schema, recursive, removeViolations);
  }

  public void visit(JsonValue instance,
          JsonClass schema,
          boolean recursive,
          boolean removeViolations) throws JsonSchemaException {
    TreeSet<String> validTypes = new TreeSet<String>();
    JsonValue type = schema.get("type");
    if (type instanceof JsonArray) {
      for (JsonValue value : ((JsonArray) type).getMembers()) {
        validTypes.add(value.stringValue());
      }
    } else {
      validTypes.add(type.stringValue());
    }
    if (instance instanceof JsonClass) {
      if (!validTypes.contains("object")
              && !validTypes.contains("map")) {
        throw new JsonSchemaException(instance.getClass().getName() + ": unexpected when schema wants [" + setToString(validTypes) + "]");
      }
      if (recursive && !schema.getBoolean("any")) {
        ArrayList<String> toRemove = new ArrayList<String>();
        JsonClass jsonClass = (JsonClass) instance;
        for (String key : jsonClass.getMembers().keySet()) {
          JsonValue subValue = jsonClass.getMembers().get(key);
          JsonValue properties = schema.get("properties");
          JsonClass subSchema = null;
          if (properties != null) {
            if (properties instanceof JsonArray) {
              // @TODO: Yeah, this won't work so well
            } else {
              subSchema = ((JsonClass) properties).getClass(key);
              if (subSchema == null && ((JsonClass) properties).getString("$ref") != null) {
                subSchema = loadSchema(((JsonClass) properties).getString("$ref"));
              }
            }
          }
          if (subSchema == null) {
            if (validTypes.contains("map")) {
              try {
                JsonClass items = schema.getClass("items");
                if (items != null) {
                  visit(subValue, items, recursive, removeViolations);
                } else {
                  if (removeViolations) {
                    toRemove.add(key);
                  } else {
                    throw (new JsonSchemaException("Tried matching a mapped value to a null item schema type"));
                  }
                }
              } catch (JsonSchemaException e) {
                if (removeViolations) {
                  toRemove.add(key);
                } else {
                  throw e;
                }
              }
            } else {
              if (removeViolations) {
                toRemove.add(key);
              } else {
                throw (new JsonSchemaException("Missing schema for property name: " + key));
              }
            }
          } else {
            try {
              visit(subValue, subSchema, recursive, removeViolations);
            } catch (JsonSchemaException e) {
              if (removeViolations) {
                toRemove.add(key);
              } else {
                throw e;
              }
            }
          }
        }
        for (String key : toRemove) {
          jsonClass.remove(key);
        }
      }
    } else if (instance instanceof JsonArray) {
      if (!validTypes.contains("array")) {
        throw new JsonSchemaException(instance.getClass().getName() + ": unexpected when schema wants [" + setToString(validTypes) + "]");
      }
      if (recursive && !schema.getBoolean("any")) {
        JsonArray jsonArray = (JsonArray) instance;
        for (int index = jsonArray.size() - 1; index >= 0; index--) {
          JsonValue subValue = jsonArray.get(index);
          JsonClass items = schema.getClass("items");
          if (items.getString("$ref") != null) {
            items = loadSchema(items.getString("$ref"));
          }
          try {
            visit(subValue, items, recursive, removeViolations);
          } catch (JsonSchemaException e) {
            if (removeViolations) {
              jsonArray.remove(index);
            } else {
              throw e;
            }
          }
        }
      }
    } else if (instance instanceof JsonString) {
      if (!validTypes.contains("string")) {
        throw new JsonSchemaException(instance.getClass().getName() + ": unexpected when schema wants [" + setToString(validTypes) + "]");
      }
    } else if (instance instanceof JsonRegex) {
      if (!validTypes.contains("regex")) {
        throw new JsonSchemaException(instance.getClass().getName() + ": unexpected when schema wants [" + setToString(validTypes) + "]");
      }
    } else if (instance instanceof JsonNumber) {
      if (((JsonNumber) instance).isIsDouble()) {
        if (!validTypes.contains("number")) {
          throw new JsonSchemaException(instance.getClass().getName() + ": unexpected when schema wants [" + setToString(validTypes) + "]");
        }
      } else {
        if (!validTypes.contains("integer")
                && !validTypes.contains("number")) {
          throw new JsonSchemaException(instance.getClass().getName() + ": unexpected when schema wants [" + setToString(validTypes) + "]");
        }
      }
    } else if (instance instanceof JsonBoolean) {
      if (!validTypes.contains("boolean")) {
        throw new JsonSchemaException(instance.getClass().getName() + ": unexpected when schema wants [" + setToString(validTypes) + "]");
      }
    }
  }

  public static String print(JsonClass jClass) {
    StringWriter sw = new StringWriter();

    print(jClass, "", sw);

    return sw.toString();
  }

  public static void print(JsonClass jClass, String indentation, Writer writer) {
    try {
      writer.write("{");
      if (jClass.getMembers().size() == 0) {
        writer.write("}");
        return;
      }
      boolean first = true;
      for (String key : jClass.getMembers().keySet()) {
        if (!first) {
          writer.write(",");
        }
        writer.write("\n");
        writer.write(indentation);
        writer.write("  ");
        writer.write("\"");
        writer.write(JsonString.jsonSanitizeRegular(key));
        writer.write("\": ");
        JsonValue value = jClass.get(key);
        if (value instanceof JsonClass) {
          print((JsonClass) value, indentation + "  ", writer);
        } else if (value instanceof JsonArray) {
          print((JsonArray) value, indentation + "  ", writer);
        } else {
          value.serializeTo(writer);
        }
        first = false;
      }
      //writer.write("\n");
      //writer.write(indentation);
      //writer.write("}");
      writer.write(" }");
    } catch (IOException e) {
    }
  }

  public static void print(JsonArray jArray, String indentation, Writer writer) {
    try {
      writer.write("[");
      if (jArray.getMembers().size() == 0) {
        writer.write("]");
        return;
      }
      boolean first = true;
      for (JsonValue value : jArray.getMembers()) {
        if (!first) {
          writer.write(",");
        }
        writer.write("\n");
        writer.write(indentation);
        writer.write("  ");
        if (value instanceof JsonClass) {
          print((JsonClass) value, indentation + "  ", writer);
        } else if (value instanceof JsonArray) {
          print((JsonArray) value, indentation + "  ", writer);
        } else {
          value.serializeTo(writer);
        }
        first = false;
      }
      //writer.write("\n");
      //writer.write(indentation);
      //writer.write("]");
      writer.write(" ]");
    } catch (IOException e) {
    }
  }
}
