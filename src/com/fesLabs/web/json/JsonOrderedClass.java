/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class JsonOrderedClass extends JsonClass
{
  private LinkedHashMap<String, JsonValue> members = new LinkedHashMap<String, JsonValue>();

  public JsonOrderedClass(){
  }

  public JsonOrderedClass(Object obj, Boolean webby){
      JsonOrderedClass jsonClass = (JsonOrderedClass) JsonSerialize.serializeUnknown(obj, webby);
      for(String key : jsonClass.members.keySet()) {
          this.members.put(key, jsonClass.members.get(key));
      }
  }

  public Map<String, JsonValue> getMembers() {
    return this.members;
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
          JsonOrderedClass classValue = new JsonOrderedClass();
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

  @Override
  public int size() {
    return this.members.size();
  }

  @Override
  public JsonOrderedClass getClass(String key) {
    JsonValue jsonValue = this.members.get(key);
    if(jsonValue != null && jsonValue instanceof JsonOrderedClass) {
      return ((JsonOrderedClass)jsonValue);
    }
    return new JsonOrderedClass();
  }

  public static JsonOrderedClass fromString(String json) {
    JsonOrderedClass jsonClass = new JsonOrderedClass();
    jsonClass.parse(json, 0);
    return jsonClass;
  }
}


