/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.fesLabs.web.json;

/**
 *
 * @author fes
 */
public interface IJsonObjectCreator {
	public Object jsonCreateObject(Class c);
	public Class jsonClassForObject(Object o);
}
