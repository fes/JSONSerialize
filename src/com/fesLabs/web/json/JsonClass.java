/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import com.fesLabs.web.json.external.Base64;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

public class JsonClass extends JsonValue implements IJsonCollection
{
  public static boolean memberNamesInQuotes = false;
  public static boolean useHashMap = false;

  //private HashMap<String, JsonValue> members = new HashMap<String, JsonValue>();
  private final Map<String, JsonValue> members;

  public JsonClass() {
    if(useHashMap) {
      members = new HashMap<String, JsonValue>();
    } else {
      members = new TreeMap<String, JsonValue>();
    }
  }

  public JsonClass(String value){
    this();
    this.parse(value, 0);
  }

  public JsonClass(String key, String value){
    this();
    this.add(key, value);
  }

  public JsonClass(String key, JsonValue value){
    this();
    this.add(key, value);
  }

  public JsonClass(Object obj, Boolean webby){
    this();
      if (obj != null){
          JsonClass jsonClass = (JsonClass) JsonSerialize.serializeUnknown(obj, webby);
          for(String key : jsonClass.members.keySet()) {
              this.members.put(key, jsonClass.members.get(key));
          }
      }
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
      if(strMember.startsWith("\"") && strMember.endsWith("\"")) {
        strMember = strMember.substring(1, strMember.length() - 2);
      }
      JsonValue value = this.get(strMember);
      if(value != null) {
        if(index < path.length()) {
          return value.via(path.substring(index + 1));
        } else {
          return value;
        }
      }
    }
    return null;
  }

  public Map<String, JsonValue> getMembers() {
    return this.members;
  }

  public void collectionAdd(String name, JsonValue value) {
    this.members.put(name, value);
  }

  public JsonClass add(String name, JsonValue value) {
    this.members.put(name, value);
    return this;
  }

  public JsonClass add(String name, long value) {
    this.members.put(name, new JsonNumber(value));
    return this;
  }

  public JsonClass add(String name, int value) {
    this.members.put(name, new JsonNumber(value));
    return this;
  }

  public JsonClass add(String name, double value) {
    this.members.put(name, new JsonNumber(value));
    return this;
  }

  public JsonClass add(String name, boolean value) {
    this.members.put(name, new JsonBoolean(value));
    return this;
  }

  public JsonClass add(String name, String value) {
    this.members.put(name, new JsonString(value));
    return this;
  }

  public JsonClass add(String name, java.util.regex.Pattern value) {
    this.members.put(name, new JsonRegex(value));
    return this;
  }

  public JsonClass addNull(String name) {
    this.members.put(name, null);
    return this;
  }

  public JsonClass remove(String name) {
    this.members.remove(name);
    return this;
  }

  public JsonClass remove(String[] names) {
    for(String name : names) {
      this.members.remove(name);
    }
    return this;
  }

  public JsonClass allowOnly(String[] name) {
    TreeSet<String> names = new TreeSet<String>();
    for(String value : name) {
      names.add(value);
    }
    Stack<String> toRemove = new Stack<String>();
    for(String key : this.members.keySet()) {
      if(!names.contains(key)) toRemove.push(key);
    }
    while(!toRemove.empty()) this.members.remove(toRemove.pop());
    return this;
  }

  protected boolean shouldQuoteKey(String key) {
    boolean forceQuotes = false;
    for(int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      if(c == '.' || c == ' ' || c == '\t' || c == '\r' || c == '\n' || c == '\'' || c == '\"' || c == '-') {
        forceQuotes = true;
        break;
      }
    }
    return forceQuotes;
  }

  @Override
  public void serializeTo(Writer writer, Set<String> skipFields) throws IOException {
    try {
    writer.write("{");
    boolean comma = false;
    for(String key : members.keySet()) {
      if(key == null) {
        continue;
      }
      if(skipFields != null && skipFields.contains(key)) {
        continue;
      }
      JsonValue value = this.members.get(key);
      if(value != null) {
        boolean forceQuotes = shouldQuoteKey(key) || key.equals("");
        if(comma) {
          writer.write(",");
        }
        comma = true;
        if(memberNamesInQuotes || forceQuotes) {
          writer.write("\"");
        }
        jsonSanitizeRegular(key, writer);
        if(memberNamesInQuotes || forceQuotes) {
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
        case '/':
          JsonRegex regexValue = new JsonRegex();
          consumed = regexValue.parse(json, offset);
          if(consumed == -1) {
            return -1;
          } else {
            this.members.put(name.getValue(), regexValue);
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
    if(jsonValue != null) {
      return jsonValue.longValue(defaultValue);
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
    } else if(jsonValue != null) {
      return jsonValue.stringValue();
    }
    return defaultValue;
  }

   public byte[] getByteArray(String key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonString) {
      try {
        return Base64.decode(((JsonString)jsonValue).getValue());
      } catch(Exception e) {}
    }
    return new byte[0];
  }

  public boolean getBoolean(String key) {
    return getBoolean(key, false);
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonBoolean) {
      return ((JsonBoolean)jsonValue).getValue();
    } else if(jsonValue != null && jsonValue instanceof JsonNumber) {
      return (((JsonNumber)jsonValue).getAsLong() != 0);
    }
    return defaultValue;
  }

  public JsonArray getArray(String key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonArray) {
      return ((JsonArray)jsonValue);
    }
    return null;
  }

  public JsonClass getClass(String key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonClass) {
      return ((JsonClass)jsonValue);
    }
    return null;
  }

  public static JsonClass fromString(String json) {
    JsonClass jsonClass = new JsonClass();
    jsonClass.parse(json, 0);
    return jsonClass;
  }
}


