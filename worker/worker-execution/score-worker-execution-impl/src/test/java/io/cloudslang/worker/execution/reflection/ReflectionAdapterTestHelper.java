/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.worker.execution.reflection;

import java.util.Map;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 20/11/2011
 * @version $Id$
 */
public class ReflectionAdapterTestHelper {

	@SuppressWarnings("unused")
	public void myMethod_1(String parameter_1, int parameter_2) {}

	@SuppressWarnings("static-method")
	public Integer myMethod_2(int parameter_1, int parameter_2) {
		return parameter_1 + parameter_2;
	}

	@SuppressWarnings({ "static-method", "unused" })
	public Map<String, ?> myMethod_3(int parameter_1, int parameter_2, Map<String, ?> executionContext) {
		return executionContext;
	}

}
