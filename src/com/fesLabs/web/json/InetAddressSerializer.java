/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import com.fesLabs.web.json.external.Base64;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public class InetAddressSerializer implements ICustomSerializer {

  protected static final int nid = 1;
  protected static final int cidInet = 1;
  protected static final int cidInet4 = 2;
  protected static final int cidInet6 = 3;

  public static void registerType() {
    JsonClassDef classDef = new JsonClassDef();
    classDef.serializer = new InetAddressSerializer();
    classDef.c = InetAddress.class;
    classDef.typeId = cidInet;
    classDef.namespaceId = nid;
    JsonSerialize.registerType(InetAddress.class, classDef);

    classDef = new JsonClassDef();
    classDef.serializer = new InetAddressSerializer();
    classDef.c = InetAddress.class;
    classDef.typeId = cidInet4;
    classDef.namespaceId = nid;
    JsonSerialize.registerType(Inet4Address.class, classDef);

    classDef = new JsonClassDef();
    classDef.serializer = new InetAddressSerializer();
    classDef.c = InetAddress.class;
    classDef.typeId = cidInet6;
    classDef.namespaceId = nid;
    JsonSerialize.registerType(Inet6Address.class, classDef);
  }

  public Object deserialize(JsonValue fromValue) {
    InetAddress address = null;

    if(fromValue instanceof JsonClass) {
      JsonClass fromClass = (JsonClass) fromValue;
      JsonValue value = fromClass.getMembers().get("bAddress");
      if(value != null && value instanceof JsonString) {
        JsonString jsonString = (JsonString) value;
        value = fromClass.getMembers().get("strHost");
        if(value != null && value instanceof JsonString) {
          try {
            address = InetAddress.getByAddress(((JsonString)value).getValue(), Base64.decode(jsonString.getValue()));
          } catch(Exception e) {}
        } else {
          try {
            address = InetAddress.getByAddress(Base64.decode(jsonString.getValue()));
          } catch(Exception e) {}
        }
      } else {
        value = fromClass.getMembers().get("strAddress");
        if(value != null && value instanceof JsonString) {
          JsonString jsonString = (JsonString) value;
          value = fromClass.getMembers().get("strHost");
          try {
            address = InetAddress.getByName((jsonString.getValue()));
          } catch(Exception e) {}
          if(address != null && value != null && value instanceof JsonString) {
            try {
              address = InetAddress.getByAddress(((JsonString)value).getValue(), address.getAddress());
            } catch(Exception e) {}
          }
        }
      }
    } else if(fromValue instanceof JsonString) {
      try {
        return InetAddress.getByName(((JsonString)fromValue).getValue());
      } catch(Exception e) {}
    }

    return address;
  }

  public JsonValue serialize(Object fromObject, boolean webby) {
    if(fromObject instanceof InetAddress) {
      InetAddress address = (InetAddress) fromObject;
      if(!webby) {
        JsonClass jsonClass = new JsonClass();
        jsonClass.add("_cid", new JsonNumber(cidInet));
        jsonClass.add("_nid", new JsonNumber(nid));
        jsonClass.add("bAddress", new JsonString(new String(Base64.encodeBytes(address.getAddress()))));
        jsonClass.add("strAddress", new JsonString(address.getHostAddress()));
        if(address.getHostName() != null) {
          jsonClass.add("strHost", new JsonString(address.getHostName()));
        }
        return jsonClass;
      } else {
        return new JsonString(address.getHostAddress());
      }
    } else {
      return null;
    }
  }

  public Object createArray(int size) {
    return new InetAddress[size];
  }

  public JsonClass getSchema(boolean webby) {
    if(webby) {
      return new JsonClass("{type:\"string\"");
    } else {
      return new JsonClass("{type:\"object\", properties:{bAddress:{type:\"string\"}, strAddress:{type:\"string\"}, strHost:{type:\"string\"}}}");
    }
  }
}
