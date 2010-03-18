/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.regex.Pattern;

public class JsonRegex extends JsonValue
{
	private Pattern value = null;

	public JsonRegex() {
	}

	public JsonRegex(String value) {
		this.value = Pattern.compile(value);
	}
  
	public JsonRegex(Pattern value) {
		this.value = value;
	}

  @Override
  public String stringValue() {
    return this.value.pattern();
  }

  @Override
  public long longValue(long defaultValue) {
    return defaultValue;
  }

	@Override
	public Object getNative() {
		return value;
	}

	public Pattern getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = Pattern.compile(value);
	}

	public void setValue(Pattern value) {
		this.value = value;
	}

  @Override
  public void serializeTo(Writer writer, Set<String> skipFields) throws IOException {
    try {
      if(this.value == null) {
        writer.write("null");
      } else {
        writer.write("/");
        jsonSanitizeRegular(this.value.pattern(), writer);
        writer.write("/");
      }
    } catch(IOException e) {
      throw e;
    }
  }

	public int parse(String json, int initialOffset) {
		int offset = initialOffset;
		int consumed;
		boolean endIsQuote = false;
		StringBuilder sb = new StringBuilder();
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
			if(c == '/') {
				offset++;
				count++;
				endIsQuote = true;
			}
		} else {
			return -1;
		}

		while(offset < json.length()) {
			c = json.charAt(offset++);
			switch(c) {
				case '/':
					if(endIsQuote) {
						count++;
						this.value = Pattern.compile(sb.toString());
						return count;
					} else {
						sb.append(c);
					}
					break;
				case '\\':
					if(offset < json.length()) {
						sb.append(c);
						count++;
						char c2 = json.charAt(offset++);
						sb.append(c2);
						count++;
					} else {
						return -1;
					}
					break;
				default:
					count++;
					sb.append(c);
					break;
			}
		}
		return -1;
	}
}

