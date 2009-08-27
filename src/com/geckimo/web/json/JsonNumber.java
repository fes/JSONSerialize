/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 Geckimo --*/

package com.geckimo.monitor.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public class JsonNumber extends JsonValue
{
	private double dValue = 0d;
	private long lValue = 0l;
	private boolean isDouble = false;

	public JsonNumber() {
	}

	public JsonNumber(double value) {
		this.dValue = value;
		this.isDouble = true;
	}

	public JsonNumber(float value) {
		this.dValue = (double)value;
		this.isDouble = true;
	}

	public JsonNumber(long value) {
		this.lValue = value;
	}

	public JsonNumber(int value) {
		this.lValue = (long) value;
	}

	public JsonNumber(short value) {
		this.lValue = (long) value;
	}

	public JsonNumber(byte value) {
		this.lValue = (long) value;
	}

	@Override
	public Object getNative() {
		if(isDouble) {
			return dValue;
		} else {
			return lValue;
		}
	}

	public long getAsLong() {
		if(isDouble) {
			return (long) dValue;
		} else {
			return lValue;
		}
	}

	public double getAsDouble() {
		if(!isDouble) {
			return (double) lValue;
		} else {
			return dValue;
		}
	}

  @Override
  public void serializeTo(Writer writer, Set<String> skipFields) throws IOException {
    try {
      writer.write(this.isDouble ? ("" + dValue) : ("" + lValue));
    } catch(IOException e) {
      throw e;
    }
  }

	public int parse(String json, int initialOffset) {
		int offset = initialOffset;
		int begin = initialOffset;
		char c;
		int count = 0;

		for(;;) {
			if(offset < json.length()) {
				c = json.charAt(offset++);
				switch(c) {
					case '.':
					case 'e':
						count++;
						setIsDouble(true);
						break;
					case ',':
					case '}':
					case ']':
					case ' ':
					case '\t':
					case '\n':
					case '\r':
						if(isIsDouble()) {
							try {
								Double d = Double.parseDouble(json.substring(begin, offset - 1));
								this.setdValue((double) d);
								return count;
							} catch(Exception e) {
								return -1;
							}
						} else {
							try {
								Long l = Long.parseLong(json.substring(begin, offset - 1));
								this.setlValue((long) l);
								return count;
							} catch(Exception e) {
								return -1;
							}
						}
					default:
						count++;
						break;
				}
			} else {
				return -1;
			}
		}
	}

	/**
	 * @return the dValue
	 */
	public double getdValue() {
		return dValue;
	}

	/**
	 * @param dValue the dValue to set
	 */
	public void setdValue(double dValue) {
		this.dValue = dValue;
	}

	/**
	 * @return the lValue
	 */
	public long getlValue() {
		return lValue;
	}

	/**
	 * @param lValue the lValue to set
	 */
	public void setlValue(long lValue) {
		this.lValue = lValue;
	}

	/**
	 * @return the isDouble
	 */
	public boolean isIsDouble() {
		return isDouble;
	}

	/**
	 * @param isDouble the isDouble to set
	 */
	public void setIsDouble(boolean isDouble) {
		this.isDouble = isDouble;
	}
}

