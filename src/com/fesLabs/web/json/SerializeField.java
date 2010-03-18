/* vim:set ts=2 sw=2 et: */
/*-- Copyright 2009 fesLabs --*/

package com.fesLabs.web.json;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerializeField {
	static int TYPE_BYTE = 0x01;
	static int TYPE_SHORT = 0x02;
	static int TYPE_INT = 0x03;
	static int TYPE_LONG = 0x04;
	static int TYPE_FLOAT = 0x05;
	static int TYPE_DOUBLE = 0x06;
	static int TYPE_BOOLEAN = 0x07;
	static int TYPE_STRING = 0x08;
	static int TYPE_BINARY = 0x09;
	static int TYPE_ARRAY = 0x0a;
	static int TYPE_ARRAYLIST = 0x0b;
	static int TYPE_TREESET = 0x0c;
	static int TYPE_TREEMAP = 0x0d;
	static int TYPE_HASHSET = 0x0e;
	static int TYPE_HASHMAP = 0x0f;
	static int TYPE_CLASS = 0x10;
	static int TYPE_CHAR = 0x11;
	static int TYPE_CUSTOM = 0x12;
	static int TYPE_JSON = 0x13;
	static int TYPE_REGEX = 0x14;
}
