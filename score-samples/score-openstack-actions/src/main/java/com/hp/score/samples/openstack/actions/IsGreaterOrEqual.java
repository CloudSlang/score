package com.hp.score.samples.openstack.actions;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

/**
 * Date: 9/11/2014.
 *
 * @author lesant
 */

public class IsGreaterOrEqual <T extends Comparable> extends BaseMatcher<T> {
	private final T object;
	public IsGreaterOrEqual(T greaterOrEqualArg) {
		object = greaterOrEqualArg;
	}
	@Override
	public boolean matches(Object arg) {
		if(object != null) {
			if (arg == null) {
				return false;
			}
			@SuppressWarnings("unchecked")
			int ret = object.compareTo(arg);
			return ret <= 0;
		}

		return arg != null;

	}
	@Override
	public void describeTo(Description description) {
		description.appendValue(object);
	}

	@Factory
	public static <T extends Comparable> Matcher<T> greaterOrEqualTo(T operand) {
		return new IsGreaterOrEqual<>(operand);
	}
}

