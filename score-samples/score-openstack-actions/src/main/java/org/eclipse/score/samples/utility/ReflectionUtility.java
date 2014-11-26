/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples.utility;

import java.lang.reflect.Method;

/**
 * Date: 8/12/2014
 *
 * @author Bonczidai Levente
 */
public class ReflectionUtility {
	public static Object  invokeMethodByName(
			String className,
			String methodName,
			Object... parameters)
			throws Exception {
		Class clazz = Class.forName(className);
		Method[] methods = clazz.getDeclaredMethods();
		Method actionMethod = null;
		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				actionMethod = m;
			}
		}
		if (actionMethod != null) {
			Object returnObject;
			returnObject = actionMethod.invoke(clazz.newInstance(), parameters);
			return returnObject;
		} else {
			throw new Exception("Method " + className + "." + methodName + " not found");
		}
	}
}
