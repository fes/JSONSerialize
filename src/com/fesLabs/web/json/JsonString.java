/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public class JsonString extends JsonValue
{
	private String value = null;
	private boolean optionalDelimiters = false;

	public JsonString() {
	}

	public JsonString(boolean optionalDelmiters) {
		this.optionalDelimiters = optionalDelmiters;
	}

	public JsonString(String value) {
		this.value = value;
	}
  
  @Override
  public String stringValue() {
    return this.value;
  }

  @Override
  public long longValue(long defaultValue) {
    if(this.value != null) {
      try {
        return Long.parseLong(this.value);
      } catch(Exception e) {}
    }
    return defaultValue;
  }

	@Override
	public Object getNative() {
		return value;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isOptionalDelmiters() {
		return this.optionalDelimiters;
	}

	public void setOptionalDelimiters(boolean optionalDelimiters) {
		this.optionalDelimiters = optionalDelimiters;
	}

  @Override
  public void serializeTo(Writer writer, Set<String> skipFields) throws IOException {
    try {
      if(this.value == null) {
        writer.write("null");
      } else {
        writer.write("\"");
        jsonSanitizeRegular(this.value, writer);
        writer.write("\"");
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
			if(c == '"') {
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
				case '"':
					if(endIsQuote) {
						count++;
						this.value = sb.toString();
						return count;
					} else {
						sb.append(c);
					}
					break;
				case ',':
				case '}':
				case ']':
				case ':':
					if(!endIsQuote && optionalDelimiters) {
						this.value = sb.toString();
						return count;
					} else {
						count++;
						sb.append(c);
					}
					break;
				case '\\':
					if(offset < json.length()) {
						count++;
						char c2 = json.charAt(offset++);
						count++;
						switch(c2) {
              /*
							case '/':
								sb.append('/');
								break;
               */
							case 'n':
								sb.append('\n');
								break;
							case 'r':
								sb.append('\n');
								break;
							case 't':
								sb.append('\t');
								break;
							case 'b':
								sb.append('\b');
								break;
							case 'f':
								sb.append('\f');
								break;
							case 'u':
								if((offset + 4) <= json.length()) {
									String val = json.substring(offset, offset + 4);
									count += 4;
									offset += 4;
									int iVal = Integer.parseInt(val, 16);
                  sb.append((char)iVal);
								} else {
									return -1;
								}
								break;
							default:
								sb.append(c2);
								break;
						}
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

