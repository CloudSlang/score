/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
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
