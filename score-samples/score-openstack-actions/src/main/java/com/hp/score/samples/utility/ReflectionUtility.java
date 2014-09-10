package com.hp.score.samples.utility;

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
