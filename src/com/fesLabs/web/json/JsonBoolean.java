  /* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public class JsonBoolean extends JsonValue
  {
    private boolean value = false;

    public JsonBoolean() {
    }

    public JsonBoolean(boolean value) {
      this.value = value;
    }

    @Override
    public Object getNative() {
      return value;
    }

    public boolean getValue() {
      return this.value;
    }

    public void setValue(boolean value) {
      this.value = value;
    }

    @Override
    public void serializeTo(Writer writer, Set<String> skipFields) throws IOException {
      try {
        writer.write(this.value ? "true" : "false");
      } catch(IOException e) {
        throw e;
      }
    }

    public int parse(String json, int initialOffset) {
      int offset = initialOffset;
      char c;

      c = json.charAt(offset);
      if(c == 't' || c == 'T') {
        if ((json.length() - offset) < 4) {
          return -1;
        } else {
          this.value = json.substring(offset, offset + 4).equalsIgnoreCase("true");
          return 4;
        }
      }
      if(c == 'f' || c == 'F') {
        if ((json.length() - offset) < 5) {
          return -1;
        } else {
          this.value = false;
          return 5;
        }
      }
      return -1;
    }
  }

