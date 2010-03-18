/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.fesLabs.web.json;

/**
 *
 * @author fes
 */
public interface IJsonSerializeCallback {
	public void jsonSerializeAdditional(Object o, JsonClass jc);
	public void jsonHandleUnknownDeserialize(String name, JsonValue value, Object o);
}
