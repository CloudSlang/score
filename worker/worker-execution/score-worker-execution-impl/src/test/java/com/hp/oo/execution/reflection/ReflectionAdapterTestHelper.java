package com.hp.oo.execution.reflection;

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
