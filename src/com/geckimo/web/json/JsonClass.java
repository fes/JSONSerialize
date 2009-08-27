/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 Geckimo --*/

package com.geckimo.monitor.json;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Set;

public class JsonClass extends JsonValue implements IJsonCollection
{
  public static boolean memberNamesInQuotes = false;

  private HashMap<String, JsonValue> members = new HashMap<String, JsonValue>();

  public JsonClass(){
  }

  public HashMap<String, JsonValue> getMembers() {
    return this.members;
  }

  public void add(String name, JsonValue value) {
    this.members.put(name, value);
  }

  public void add(String name, long value) {
    this.members.put(name, new JsonNumber(value));
  }

  public void add(String name, int value) {
    this.members.put(name, new JsonNumber(value));
  }

  public void add(String name, double value) {
    this.members.put(name, new JsonNumber(value));
  }

  public void add(String name, boolean value) {
    this.members.put(name, new JsonBoolean(value));
  }

  public void add(String name, String value) {
    this.members.put(name, new JsonString(value));
  }

  public void addNull(String name) {
    this.members.put(name, null);
  }

  @Override
  public void serializeTo(Writer writer, Set<String> skipFields) throws IOException {
    try {
    writer.write("{");
    boolean comma = false;
    for(String key : members.keySet()) {
      if(skipFields != null && skipFields.contains(key)) {
        continue;
      }
      JsonValue value = this.members.get(key);
      if(value != null) {
        if(comma) {
          writer.write(",");
        }
        comma = true;
        if(memberNamesInQuotes) {
          writer.write("\"");
        }
        jsonSanitizeRegular(key, writer);
        if(memberNamesInQuotes) {
          writer.write("\"");
        }
        writer.write(":");
        value.serializeTo(writer, skipFields);
      }
    }
    writer.write("}");
    } catch(IOException e) {
      throw(e);
    }
  }

  public int parse(String json, int initialOffset) {
//System.out.println("JsonClass: parse " + initialOffset);
    int offset = initialOffset;
    int consumed;
    int count = 0;

    // Whitespace
    consumed = collectWhitespace(json, offset);
    if(consumed == -1) {
      return -1;
    }
    offset += consumed;
    count += consumed;

    char c;
    if(offset < json.length()) {
      c = json.charAt(offset);
      if(c == '{') {
        offset++;
        count++;
      } else {
        return -1;
      }
    } else {
      return -1;
    }
    while(offset < json.length()) {
      // Whitespace
      consumed = collectWhitespace(json, offset);
      if(consumed == -1) {
        return -1;
      }
      offset += consumed;
      count += consumed;

      // Member name
      JsonString name = new JsonString(true);
      consumed = name.parse(json, offset);
      if(consumed == -1) {
        return -1;
      }
      offset += consumed;
      count += consumed;

//System.out.println("JsonClass: parse member " + name.getValue() + " at " + initialOffset);
      // Whitespace
      consumed = collectWhitespace(json, offset);
      if(consumed == -1) {
        return -1;
      }
      offset += consumed;
      count += consumed;

      if(!(offset < json.length())) {
        return -1;
      }
      c = json.charAt(offset++);
      switch(c) {
        case ':':
          count++;
          consumed = parseUnknown(json, offset, name);
          if(consumed == -1) {
            return -1;
          } else {
            offset += consumed;
            count += consumed;
          }
          break;
        case '}':
          count++;
          return count;
        default:
          return -1;
      }

      // Whitespace
      consumed = collectWhitespace(json, offset);
      if(consumed == -1) {
        return -1;
      }
      offset += consumed;
      count += consumed;

      if(!(offset < json.length())) {
        return -1;
      }
      c = json.charAt(offset++);
      switch(c) {
        case ',':
          count++;
          break;
        case '}':
          count++;
          return count;
        default:
          return -1;
      }
    }
    return -1;
  }

  protected int parseUnknown(String json, int initialOffset, JsonString name) {
    int offset = initialOffset;
    char c;
    int consumed;
    StringBuilder sb = new StringBuilder();
    int count = 0;

    // Whitespace
    consumed = collectWhitespace(json, offset);
    if(consumed == -1) {
      return -1;
    }
    offset += consumed;
    count += consumed;

    if(offset < json.length()) {
      c = json.charAt(offset);
      switch(c) {
        case '"':
          JsonString stringValue = new JsonString(false);
          consumed = stringValue.parse(json, offset);
          if(consumed == -1) {
            return -1;
          } else {
            this.members.put(name.getValue(), stringValue);
            offset += consumed;
            count += consumed;
            return count;
          }
        case '{':
          JsonClass classValue = new JsonClass();
          consumed = classValue.parse(json, offset);
          if(consumed == -1) {
            return -1;
          } else {
            this.members.put(name.getValue(), classValue);
            offset += consumed;
            count += consumed;
            return count;
          }
        case '[':
          JsonArray arrayValue = new JsonArray();
          consumed = arrayValue.parse(json, offset);
          if(consumed == -1) {
            return -1;
          } else {
            this.members.put(name.getValue(), arrayValue);
            offset += consumed;
            count += consumed;
            return count;
          }
        case 't':
        case 'f':
        case 'T':
        case 'F':
          JsonBoolean booleanValue = new JsonBoolean();
          consumed = booleanValue.parse(json, offset);
          if(consumed == -1) {
            return -1;
          } else {
            this.members.put(name.getValue(), booleanValue);
            offset += consumed;
            count += consumed;
            return count;
          }
        case 'n':
        case 'N':
          if((offset + 4) < json.length()) {
            offset += 4;
            count += 4;
            return count;
          }
        default:
          JsonNumber numberValue = new JsonNumber();
          consumed = numberValue.parse(json, offset);
          if(consumed == -1) {
            return -1;
          } else {
            this.members.put(name.getValue(), numberValue);
            offset += consumed;
            count += consumed;
            return count;
          }
      }
    }
    return -1;
  }

  public JsonValue get(String key) {
    return this.members.get(key);
  }

  public int size() {
    return this.members.size();
  }

  public int getInt(String key, int defaultValue) {
    return (int) getLong(key, (long) defaultValue);
  }

  public long getLong(String key, long defaultValue) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonNumber) {
      return ((JsonNumber)jsonValue).getAsLong();
    } else if(jsonValue != null && jsonValue instanceof JsonString) {
      try {
        return Long.parseLong(((JsonString)jsonValue).getValue());
      } catch(Exception e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public double getDouble(String key, double defaultValue) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonNumber) {
      return ((JsonNumber)jsonValue).getAsDouble();
    } else if(jsonValue != null && jsonValue instanceof JsonString) {
      try {
        return Double.parseDouble(((JsonString)jsonValue).getValue());
      } catch(Exception e) {
        return defaultValue;
      }
    }
    return defaultValue;
  }

  public String getString(String key) {
    return getString(key, null);
  }

  public String getString(String key, String defaultValue) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonString) {
      return ((JsonString)jsonValue).getValue();
    }
    return defaultValue;
  }

  public boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonBoolean) {
      return ((JsonBoolean)jsonValue).getValue();
    }
    return defaultValue;
  }

  public JsonArray getArray(String key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonArray) {
      return ((JsonArray)jsonValue);
    }
    return new JsonArray();
  }

  public JsonClass getClass(String key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonClass) {
      return ((JsonClass)jsonValue);
    }
    return new JsonClass();
  }

  public static JsonClass fromString(String json) {
    JsonClass jsonClass = new JsonClass();
    jsonClass.parse(json, 0);
    return jsonClass;
  }
}


