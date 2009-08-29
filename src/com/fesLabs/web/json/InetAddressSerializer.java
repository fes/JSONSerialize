/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import com.fesLabs.web.json.external.Base64;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

public class InetAddressSerializer implements ICustomSerializer {
  public static void registerType() {
    JsonSerialize.JsonClassDef classDef = new JsonSerialize.JsonClassDef();
    classDef.serializer = new InetAddressSerializer();
    classDef.c = InetAddress.class;
    classDef.typeId = 1;
    classDef.namespaceId = 101;
    JsonSerialize.registerType(InetAddress.class, classDef);

    classDef = new JsonSerialize.JsonClassDef();
    classDef.serializer = new InetAddressSerializer();
    classDef.c = InetAddress.class;
    classDef.typeId = 2;
    classDef.namespaceId = 101;
    JsonSerialize.registerType(Inet4Address.class, classDef);

    classDef = new JsonSerialize.JsonClassDef();
    classDef.serializer = new InetAddressSerializer();
    classDef.c = InetAddress.class;
    classDef.typeId = 3;
    classDef.namespaceId = 101;
    JsonSerialize.registerType(Inet6Address.class, classDef);
  }

  public Object deserialize(JsonClass fromClass) {
    InetAddress address = null;

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

    return address;
  }

  public JsonClass serialize(Object fromObject, boolean webby) {
    if(fromObject instanceof InetAddress) {
      InetAddress address = (InetAddress) fromObject;
      JsonClass jsonClass = new JsonClass();
      if(!webby) {
        jsonClass.add("_cid", new JsonNumber(1));
        jsonClass.add("_nid", new JsonNumber(101));
        jsonClass.add("bAddress", new JsonString(new String(Base64.encode(address.getAddress()))));
      }
      jsonClass.add("strAddress", new JsonString(address.getHostAddress()));
      if(address.getHostName() != null) {
        jsonClass.add("strHost", new JsonString(address.getHostName()));
      }
      return jsonClass;
    } else {
      return null;
    }
  }

  public Object createArray(int size) {
    return new InetAddress[size];
  }
}
