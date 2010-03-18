/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/
package com.fesLabs.web.json;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.TreeSet;

public class JsonFieldDef {

  public Field field = null;
  public Method getter = null;
  public Method setter = null;
  public String name = null;
  public int type = -1;
  public JsonTypeDef typeDef = null;
  public TreeSet<Integer> schemaLevels = null;
  public TreeSet<Class> schemaTypes = null;
  public String schemaDescription = null;

  public JsonFieldDef(Field field, String name, int type) {
    this.field = field;
    this.name = name;
    this.type = type;
    this.typeDef = new JsonTypeDef(field);
    setupAnnotations();
    Class c = field.getDeclaringClass();
    Type genericType = this.field.getGenericType();
    String strGetter = "get" + ("" + name.charAt(0)).toUpperCase() + name.substring(1);
    String strSetter = "set" + ("" + name.charAt(0)).toUpperCase() + name.substring(1);
    if (genericType.equals(Boolean.class) || genericType.toString().equals("boolean")) {
      strGetter = "is" + ("" + name.charAt(0)).toUpperCase() + name.substring(1);
    }
    try {
      getter = c.getMethod(strGetter, (java.lang.Class[]) null);
    } catch (Exception e) {
    }
    try {
      setter = c.getMethod(strSetter, this.field.getType());
    } catch (Exception e) {
    }
  }

  private void setupAnnotations() {
    SchemaLevelField sl = field.getAnnotation(SchemaLevelField.class);
    if (sl != null) {
      this.schemaLevels = new TreeSet<Integer>();
      int[] levels = sl.value();
      for (int level : levels) {
        this.schemaLevels.add(level);
      }
    } else {
      SchemaLevelClass slc = field.getDeclaringClass().getAnnotation(SchemaLevelClass.class);
      if (slc != null) {
        this.schemaLevels = new TreeSet<Integer>();
        int[] levels = slc.value();
        for (int level : levels) {
          this.schemaLevels.add(level);
        }
      }
    }
    SchemaAllowedTypes st = field.getAnnotation(SchemaAllowedTypes.class);
    if (st != null) {
      this.schemaTypes = new TreeSet<Class>();
      Class[] classNames = st.value();
      for (Class className : classNames) {
        this.schemaTypes.add(className);
      }
    }
    SchemaDescription sd = field.getAnnotation(SchemaDescription.class);
    if (sd != null) {
      this.schemaDescription = sd.value();
    }
  }

  @Override
  public String toString() {
    return toString("");
  }

  public String toString(String indent) {
    StringBuilder sb = new StringBuilder();
    sb.append(indent);
    sb.append(name);
    sb.append(": ");
    sb.append(this.typeDef.toString(indent + "  "));
    return sb.toString();
  }

  public boolean forSchemaLevel(int level) {
    if (level == 0) {
      return true;
    }
    if (this.schemaLevels == null) {
      return true;
    }
    return this.schemaLevels.contains(level);
  }

  public void setValueAsObject(Object o, Object v) throws IllegalAccessException, InvocationTargetException {
    if (this.setter != null) {
      this.setter.invoke(o, new Object[]{v});
    } else {
      this.field.set(o, v);
    }
  }

  public void setValueAsByte(Object o, Byte v) throws IllegalAccessException, InvocationTargetException {
    if (this.setter != null) {
      this.setter.invoke(o, new Object[]{v});
    } else {
      this.field.set(o, v);
    }
  }

  public void setValueAsShort(Object o, Short v) throws IllegalAccessException, InvocationTargetException {
    if (this.setter != null) {
      this.setter.invoke(o, new Object[]{v});
    } else {
      this.field.set(o, v);
    }
  }

  public void setValueAsInt(Object o, Integer v) throws IllegalAccessException, InvocationTargetException {
    if (this.setter != null) {
      this.setter.invoke(o, new Object[]{v});
    } else {
      this.field.set(o, v);
    }
  }

  public void setValueAsLong(Object o, Long v) throws IllegalAccessException, InvocationTargetException {
    if (this.setter != null) {
      this.setter.invoke(o, new Object[]{v});
    } else {
      this.field.set(o, v);
    }
  }

  public void setValueAsFloat(Object o, Float v) throws IllegalAccessException, InvocationTargetException {
    if (this.setter != null) {
      this.setter.invoke(o, new Object[]{v});
    } else {
      this.field.set(o, v);
    }
  }

  public void setValueAsDouble(Object o, Double v) throws IllegalAccessException, InvocationTargetException {
    if (this.setter != null) {
      this.setter.invoke(o, new Object[]{v});
    } else {
      this.field.set(o, v);
    }
  }

  public void setValueAsBoolean(Object o, Boolean v) throws IllegalAccessException, InvocationTargetException {
    if (this.setter != null) {
      this.setter.invoke(o, new Object[]{v});
    } else {
      this.field.set(o, v);
    }
  }

  public void setValueAsRegex(Object o, java.util.regex.Pattern v) throws IllegalAccessException, InvocationTargetException {
    if (this.setter != null) {
      this.setter.invoke(o, new Object[]{v});
    } else {
      this.field.set(o, v);
    }
  }

  public Object getValueAsObject(Object o) throws IllegalAccessException, InvocationTargetException {
    if (this.getter != null) {
      return this.getter.invoke(o, new Object[0]);
    } else {
      return this.field.get(o);
    }
  }

  public Byte getValueAsByte(Object o) throws IllegalAccessException, InvocationTargetException {
    try {
      if (this.getter != null) {
        return (Byte) this.getter.invoke(o, new Object[0]);
      } else {
        if (this.typeDef.primitive) {
          return this.field.getByte(o);
        } else {
          return (Byte) this.field.get(o);
        }
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Short getValueAsShort(Object o) throws IllegalAccessException, InvocationTargetException {
    try {
      if (this.getter != null) {
        return (Short) this.getter.invoke(o, new Object[0]);
      } else {
        if (this.typeDef.primitive) {
          return this.field.getShort(o);
        } else {
          return (Short) this.field.get(o);
        }
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Integer getValueAsInt(Object o) throws IllegalAccessException, InvocationTargetException {
    try {
      if (this.getter != null) {
        return (Integer) this.getter.invoke(o, new Object[0]);
      } else {
        if (this.typeDef.primitive) {
          return this.field.getInt(o);
        } else {
          return (Integer) this.field.get(o);
        }
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Long getValueAsLong(Object o) throws IllegalAccessException, InvocationTargetException {
    try {
      if (this.getter != null) {
        return (Long) this.getter.invoke(o, new Object[0]);
      } else {
        if (this.typeDef.primitive) {
          return this.field.getLong(o);
        } else {
          return (Long) this.field.get(o);
        }
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Float getValueAsFloat(Object o) throws IllegalAccessException, InvocationTargetException {
    try {
      if (this.getter != null) {
        return (Float) this.getter.invoke(o, new Object[0]);
      } else {
        if (this.typeDef.primitive) {
          return this.field.getFloat(o);
        } else {
          return (Float) this.field.get(o);
        }
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Double getValueAsDouble(Object o) throws IllegalAccessException, InvocationTargetException {
    try {
      if (this.getter != null) {
        return (Double) this.getter.invoke(o, new Object[0]);
      } else {
        if (this.typeDef.primitive) {
          return this.field.getDouble(o);
        } else {
          return (Double) this.field.get(o);
        }
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Boolean getValueAsBoolean(Object o) throws IllegalAccessException, InvocationTargetException {
    try {
      if (this.getter != null) {
        return (Boolean) this.getter.invoke(o, new Object[0]);
      } else {
        if (this.typeDef.primitive) {
          return this.field.getBoolean(o);
        } else {
          return (Boolean) this.field.get(o);
        }
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }

  public java.util.regex.Pattern getValueAsRegex(Object o) throws IllegalAccessException, InvocationTargetException {
    try {
      if (this.getter != null) {
        return (java.util.regex.Pattern) this.getter.invoke(o, new Object[0]);
      } else {
        return (java.util.regex.Pattern) this.field.get(o);
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      return null;
    }
  }
}
