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

public class JsonArray extends JsonValue implements IJsonCollection, Iterable<JsonValue>
{
  private ArrayList<JsonValue> members = new ArrayList<JsonValue>();

  public JsonArray() {
  }

  public JsonArray(String value) {
    this.parse(value, 0);
  }

  public JsonArray(List values, boolean webby) {
      if (values != null){
          for(Object value : values) {
              JsonValue jsonValue = JsonSerialize.serializeUnknown(value, webby);
              this.members.add(jsonValue);
          }
      }
  }

  public JsonArray(Map map, boolean webby) {
      
      JsonValue jsonValue = JsonSerialize.serializeUnknown(map, webby);
      this.members.add(jsonValue);

  }

  public JsonArray(Set set, boolean webby) {
      JsonValue jsonValue = JsonSerialize.serializeUnknown(set, webby);
      this.members.add(jsonValue);
  }

  @Override
  public JsonValue via(String path) {
    StringBuffer part = new StringBuffer();
    int index = 0;
    boolean done = false;
    for(; index < path.length(); index++) {
      char c = path.charAt(index);
      switch(c) {
        case '.':
        case '[':
        case ']':
          if(c == ']') index++;
          done = true;
          break;
        case '\\':
          index++;
          if(index < path.length()) {
            part.append(path.charAt(index));
          }
          break;
        default:
          part.append(c);
          break;
      }
      if(done) {
        break;
      }
    }
    if(part.length() > 0) {
      String strMember = part.toString();
      try {
        if(strMember.equals("length")) {
          return new JsonNumber(this.members.size());
        } else {
          int memberIndex = Integer.parseInt(strMember);
          if(memberIndex < this.members.size()) {
            JsonValue value = this.members.get(memberIndex);
            if(value != null) {
              if(index < path.length()) {
                return value.via(path.substring(index + 1));
              } else {
                return value;
              }
            }
          }
        }
      } catch(Exception e) {
      }
    }
    return null;
  }

  public Iterator<JsonValue> iterator() {
      return members.iterator();
  }

  public int size() {
    return this.members.size();
  }

  public JsonValue get(int index) {
    if(index < 0 || index >= this.members.size()) {
      return null;
    }
    return this.members.get(index);
  }

  public ArrayList<JsonValue> getAll(){
      return this.members;
  }

  public void collectionAdd(String name, JsonValue value) {
    this.members.add(value);
  }

  public JsonArray add(JsonValue value) {
    this.members.add(value);
    return this;
  }

  public JsonArray add(long value) {
    this.members.add(new JsonNumber(value));
    return this;
  }

  public JsonArray add(int value) {
    this.members.add(new JsonNumber(value));
    return this;
  }

  public JsonArray add(double value) {
    this.members.add(new JsonNumber(value));
    return this;
  }

  public JsonArray add(boolean value) {
    this.members.add(new JsonBoolean(value));
    return this;
  }

  public JsonArray add(String value) {
    this.members.add(new JsonString(value));
    return this;
  }

  public JsonArray add(java.util.regex.Pattern value) {
    this.members.add(new JsonRegex(value));
    return this;
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
        case '/':
          JsonRegex regexValue = new JsonRegex();
          consumed = regexValue.parse(json, offset);
          if(consumed == -1) {
            return -1;
          } else {
            this.members.add(regexValue);
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
        case 'n':
        {
          JsonString nullValue = new JsonString(true);
          consumed = nullValue.parse(json, offset);
          System.err.println("!!!! here: " + consumed);
          if(consumed == -1) {
            return -1;
          } else {
            offset += consumed;
            count += consumed;
            return count;
          }
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
    if(jsonValue != null) {
      return jsonValue.longValue(defaultValue);
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
    } else if(jsonValue != null) {
      return jsonValue.stringValue();
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
    } else if(jsonValue != null && jsonValue instanceof JsonNumber) {
      return (((JsonNumber)jsonValue).getAsLong() != 0);
    }
    return defaultValue;
  }

  public JsonArray getArray(int key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonArray) {
      return ((JsonArray)jsonValue);
    }
    return null;
  }

  public JsonClass getClass(int key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonClass) {
      return ((JsonClass)jsonValue);
    }
    return null;
  }

  public static JsonArray fromString(String json) {
    JsonArray jsonArray = new JsonArray();
    jsonArray.parse(json, 0);
    return jsonArray;
  }
}

