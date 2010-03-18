/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fesLabs.web.json.mongo;

import com.fesLabs.web.json.JsonClass;
import java.beans.*;
import java.lang.reflect.*;
import net.sf.cglib.proxy.*;

/**
 *
 * @author fes
 */
public class MongoObjectWrapper implements MethodInterceptor, IJsonAdditional {

	public static Object newInstance(Class c) {
		try {
			MongoObjectWrapper interceptor = new MongoObjectWrapper();
			interceptor.wrapped = c;
			Enhancer e = new Enhancer();
			e.setSuperclass(c);
			e.setCallback(interceptor);
			e.setInterfaces(new Class[] {IJsonAdditional.class});
			Object result = e.create();
			interceptor.propertySupport = new PropertyChangeSupport(result);
			return result;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new Error(e.getMessage());
		}

	}

	private PropertyChangeSupport propertySupport;
	private JsonClass additional = new JsonClass();
	private Class wrapped = null;

	public JsonClass getJsonAdditional() {
		return additional;
	}

	public Class getWrapped() {
		return wrapped;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}

	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Object retValFromSuper = null;
		try {
			if(method.getDeclaringClass().equals(IJsonAdditional.class)) {
				retValFromSuper = method.invoke(this, args);
			} else {
				if (!Modifier.isAbstract(method.getModifiers())) {
					retValFromSuper = proxy.invokeSuper(obj, args);
				}
			}
		} finally {
			String name = method.getName();
			if (name.equals("addPropertyChangeListener")) {
				addPropertyChangeListener((PropertyChangeListener) args[0]);
			} else if (name.equals("removePropertyChangeListener")) {
				removePropertyChangeListener((PropertyChangeListener) args[0]);
			}
			if (name.startsWith("set") &&
				args.length == 1 &&
				method.getReturnType() == Void.TYPE) {

				char propName[] = name.substring("set".length()).toCharArray();

				propName[0] = Character.toLowerCase(propName[0]);
				propertySupport.firePropertyChange(new String(propName), null, args[0]);

			}
		}
		return retValFromSuper;
	}
}
