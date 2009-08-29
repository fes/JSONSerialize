/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;

public class JsonArray extends JsonValue implements IJsonCollection //, Iterator<JsonValue>
{
  private ArrayList<JsonValue> members = new ArrayList<JsonValue>();

  public JsonArray() {
  }

  public JsonArray(List values, boolean webby) {
      for(Object value : values) {
          JsonValue jsonValue = JsonSerialize.serializeUnknown(value, webby);
          this.members.add(jsonValue);
      }
  }

  public JsonArray(Map map, boolean webby) {
      
      JsonValue jsonValue = JsonSerialize.serializeUnknown(map, webby);
      this.members.add(jsonValue);

  }

/*
  public Iterator<JsonValue> iterator() {
      return members.iterator();
  }
*/
  public int size() {
    return this.members.size();
  }

  public JsonValue get(int index) {
    if(index < 0 || index >= this.members.size()) {
      return null;
    }
    return this.members.get(index);
  }

  public void add(JsonValue value) {
    this.members.add(value);
  }

  public void add(long value) {
    this.members.add(new JsonNumber(value));
  }

  public void add(int value) {
    this.members.add(new JsonNumber(value));
  }

  public void add(double value) {
    this.members.add(new JsonNumber(value));
  }

  public void add(boolean value) {
    this.members.add(new JsonBoolean(value));
  }

  public void add(String value) {
    this.members.add(new JsonString(value));
  }

  public void add(String name, JsonValue value) {
    this.members.add(value);
  }

  public void remove(int index) {
    if(index >= 0 && index < this.members.size()) {
      this.members.remove(index);
    }
  }

  public ArrayList<JsonValue> getMembers() {
    return this.members;
  }

  @Override
  public void serializeTo(Writer writer, Set<String> skipFields) throws IOException {
    try {
      writer.write("[");
      boolean comma = false;
      for(JsonValue value : members) {
        if(comma) {
          writer.write(",");
        }
        comma = true;
        value.serializeTo(writer, skipFields);
      }
      writer.write("]");
    } catch(IOException e) {
      throw e;
    }
  }

  public int parse(String json, int initialOffset) {
//System.out.println("JsonArray: parse " + initialOffset);
    int offset = initialOffset;
    int consumed;
    char c;
    int count = 0;

    offset++; // [
    count++;

    for(;;) {
      consumed = collectWhitespace(json, offset);
      if(consumed == -1) {
        return -1;
      }
      offset += consumed;
      count += consumed;

      if(offset < json.length()) {
        c = json.charAt(offset);
        switch(c) {
          case ',':
            offset++;
            count++;
            break;
          case ']':
            offset++;
            count++;
            return count;
          default:
            consumed = parseUnknown(json, offset);
            if(consumed == -1) {
              return -1;
            }
            offset += consumed;
            count += consumed;
            break;
        }
      } else {
        return -1;
      }
    }
  }

  protected int parseUnknown(String json, int initialOffset) {
    int offset = initialOffset;
    char c;
    int consumed;
    StringBuilder sb = new StringBuilder();
    int count = 0;

    if(offset < json.length()) {
      c = json.charAt(offset);
      switch(c) {
        case '"':
          JsonString stringValue = new JsonString(false);
          consumed = stringValue.parse(json, offset);
          if(consumed == -1) {
            return -1;
          } else {
            this.members.add(stringValue);
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
            this.members.add(classValue);
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
            this.members.add(arrayValue);
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
            this.members.add(booleanValue);
            offset += consumed;
            count += consumed;
            return count;
          }
        default:
          JsonNumber numberValue = new JsonNumber();
          consumed = numberValue.parse(json, offset);
          if(consumed == -1) {
            return -1;
          } else {
            this.members.add(numberValue);
            offset += consumed;
            count += consumed;
            return count;
          }
      }
    }
    return -1;
  }

  public long getLong(int key, long defaultValue) {
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

  public double getDouble(int key, double defaultValue) {
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

  public String getString(int key) {
    return getString(key, null);
  }

  public String getString(int key, String defaultValue) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonString) {
      return ((JsonString)jsonValue).getValue();
    }
    return defaultValue;
  }

  public boolean getBoolean(int key) {
    return getBoolean(key, false);
  }

  public boolean getBoolean(int key, boolean defaultValue) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonBoolean) {
      return ((JsonBoolean)jsonValue).getValue();
    }
    return defaultValue;
  }

  public JsonArray getArray(int key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonArray) {
      return ((JsonArray)jsonValue);
    }
    return new JsonArray();
  }

  public JsonClass getClass(int key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonClass) {
      return ((JsonClass)jsonValue);
    }
    return new JsonClass();
  }

  public static JsonArray fromString(String json) {
    JsonArray jsonArray = new JsonArray();
    jsonArray.parse(json, 0);
    return jsonArray;
  }
}

