/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

public class JsonValue
{
	public Object getNative() {
		return null;
	}

  public JsonClass viaClass(String path) {
    JsonValue value = via(path);
    if(value != null && value instanceof JsonClass) {
      return (JsonClass) value;
    }
    return null;
  }

  public JsonArray viaArray(String path) {
    JsonValue value = via(path);
    if(value != null && value instanceof JsonArray) {
      return (JsonArray) value;
    }
    return null;
  }

  public boolean viaBoolean(String path) {
    JsonValue value = via(path);
    if(value != null) {
      if(value instanceof JsonBoolean) {
        return ((JsonBoolean)value).getValue();
      } else if(value instanceof JsonNumber) {
        return (((JsonNumber)value).getAsLong() != 0l);
      } else if(value instanceof JsonString) {
        return (((JsonString)value).getValue() != null && ((JsonString)value).getValue().equalsIgnoreCase("true"));
      }
    }
    return false;
  }

  public int viaInt(String path, int defaultValue) {
    JsonValue value = via(path);
    if(value != null) {
      return (int)value.longValue(defaultValue);
    }
    return defaultValue;
  }

  public long viaLong(String path, long defaultValue) {
    JsonValue value = via(path);
    if(value != null) {
      return value.longValue(defaultValue);
    }
    return defaultValue;
  }

  public String viaString(String path) {
    JsonValue value = via(path);
    if(value != null) {
      return value.stringValue();
    }
    return null;
  }

  public JsonValue via(String path) {
    if(path == null || path.equals("")) {
      return this;
    }
    return null;
  }

  public String stringValue() {
    return null;
  }

  public long longValue(long defaultValue) {
    return defaultValue;
  }

	protected int collectWhitespace(String json, int initialOffset) {
		int offset = initialOffset;
		int count = 0;
		for(;;) {
			if(offset < json.length()) {
				char c = json.charAt(offset++);
				if(c == ' ' || c == '\t' || c == '\n' || c == '\r') {
					count++;
				} else {
					return count;
				}
			} else {
				return -1;
			}
		}
	}

  public static boolean convertWhitespace = true;

	public static String jsonSanitize(String value) {
    return jsonSanitizeRegular(value);
  }

	public static String jsonSanitizeRegular(String value) {
    StringWriter sw = new StringWriter();
    try {
      jsonSanitizeRegular(value, sw);
    } catch(Exception e) {
      return null;
    }
    sw.flush();
    return sw.getBuffer().toString();
  }

	public static void jsonSanitizeRegular(String value, Writer writer) throws IOException {
    try {
      for(int index = 0; index < value.length(); index++) {
        char c = value.charAt(index);
        switch(c) {
          case '"':
            writer.write("\\\"");
            break;
          case '\\':
            writer.write("\\\\");
            break;
          case '\b':
            if(JsonValue.convertWhitespace) {
              writer.write("\\b");
            } else {
              writer.write(c);
            }
            break;
          case '\f':
            if(JsonValue.convertWhitespace) {
              writer.write("\\f");
            } else {
              writer.write(c);
            }
            break;
          case '\n':
            if(JsonValue.convertWhitespace) {
              writer.write("\\n");
            } else {
              writer.write(c);
            }
            break;
          case '\r':
            if(JsonValue.convertWhitespace) {
              writer.write("\\r");
            } else {
              writer.write(c);
            }
            break;
          case '\t':
            if(JsonValue.convertWhitespace) {
              writer.write("\\t");
            } else {
              writer.write(c);
            }
            break;
          default:
            writer.write(c);
            break;
        }
      }
    } catch(IOException e) {
      throw e;
    }
  }

	public static String jsonSanitizeCautious(String value) {
		StringBuffer sb = new StringBuffer();
		try {
			byte[] bytes = value.getBytes("UTF-16LE");
			for(int index = 0; (index + 1) < bytes.length; index += 2) {
				int s = (((bytes[index]) & 0x00ff) | ((bytes[index + 1] << 8) & 0x00ff00));
				if((((s < 0x20 || s > 0x7a) && (s != 0x0d && s != 0x0a))
                || s == 0x26
                || s == 0x3a
                || s == 0x3c
                || s == 0x3e
                || s == 0x40
                || s == 0x22)) {
					String hex = Integer.toHexString(s);
					sb.append("\\u");
					for(int i = 0; i < (4 - hex.length()); i++) {
						sb.append("0");
					}
					sb.append(hex);
				} else {
					char c = (char)s;
					if(c == '\n') {
						sb.append("\\n");
					} else if(c == '\r') {
						sb.append("\\r");
					} else if(c == '\t') {
						sb.append("\\t");
					} else if(c == '"') {
						sb.append("\\\"");
					} else if(c == '\\') {
						sb.append("\\\\");
					} else {
						sb.append(c);
					}
				}
			}
		} catch(Exception e) {}
		return sb.toString();
	}


  public void serializeTo(Writer writer) throws IOException {
    serializeTo(writer, null);
  }

  public void serializeTo(Writer writer, Set<String> fields) throws IOException {
  }

  @Override
  public String toString() {
    StringWriter sw = new StringWriter();
    try {
      serializeTo(sw);
    } catch(Exception e) {
      return null;
    }
    sw.flush();
    return sw.getBuffer().toString();
  }

  public String toStringSkipFields(Set<String> fields) {
    StringWriter sw = new StringWriter();
    try {
      serializeTo(sw, fields);
    } catch(Exception e) {
      return null;
    }
    sw.flush();
    return sw.getBuffer().toString();
  }
}

