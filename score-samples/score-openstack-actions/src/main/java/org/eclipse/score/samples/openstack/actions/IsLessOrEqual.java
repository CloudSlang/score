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
package org.eclipse.score.samples.openstack.actions;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * Date: 9/11/2014.
 *
 * @author lesant
 */


public class IsLessOrEqual <T extends Comparable> extends BaseMatcher<T> {
	private final T object;
	public IsLessOrEqual(T lessOrEqualArg) {
		object = lessOrEqualArg;
	}
	@Override
	public boolean matches(Object arg) {
		if(object != null) {
			if (arg == null) {
				return false;
			}
			@SuppressWarnings("unchecked")
			int ret = object.compareTo(arg);
			return ret >= 0;
		}

		return arg != null;

	}
	@Override
	public void describeTo(Description description) {
		description.appendValue(object);
	}

	@Factory
	public static <T extends Comparable> Matcher<T> lessOrEqualTo(T operand) {
		return new IsLessOrEqual<>(operand);
	}
}