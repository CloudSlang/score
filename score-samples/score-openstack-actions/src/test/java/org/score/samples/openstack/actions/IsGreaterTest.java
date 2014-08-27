package org.score.samples.openstack.actions;

import org.hamcrest.Matcher;
import org.junit.Test;
import static org.score.samples.openstack.actions.IsGreater.greaterThan;

import static org.junit.Assert.assertEquals;

/**
 * Date: 8/27/2014.
 *
 * @author lesant
 */
public class IsGreaterTest {
	private static final long DEFAULT_TIMEOUT = 5000;
	private static final String RESULT_MESSAGE = "Result not as expected.";

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testGreaterThan(){

		Matcher matcher = greaterThan("20");

		assertEquals(RESULT_MESSAGE, false, matcher.matches("19"));
		assertEquals(RESULT_MESSAGE, true, matcher.matches("21"));
		assertEquals(RESULT_MESSAGE, false, matcher.matches("20"));
		assertEquals(RESULT_MESSAGE, true, matcher.matches("Hello"));
		assertEquals(RESULT_MESSAGE, false, matcher.matches("-1"));
		assertEquals(RESULT_MESSAGE, false, matcher.matches(null));
		matcher = greaterThan(null);
		assertEquals(RESULT_MESSAGE, true, matcher.matches("1"));
		assertEquals(RESULT_MESSAGE, false, matcher.matches(null));
	}

}
