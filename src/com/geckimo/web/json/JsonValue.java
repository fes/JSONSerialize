/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 Geckimo --*/

package com.geckimo.monitor.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Set;

public class JsonValue
{
	public Object getNative() {
		return null;
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
            writer.write("\\b");
            break;
          case '\f':
            writer.write("\\f");
            break;
          case '\n':
            writer.write("\\n");
            break;
          case '\r':
            writer.write("\\r");
            break;
          case '\t':
            writer.write("\\t");
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

