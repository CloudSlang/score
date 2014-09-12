package com.hp.score.samples.openstack.actions;

import org.apache.commons.lang.Validate;
import org.hamcrest.Matcher;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.anything;
import static com.hp.score.samples.openstack.actions.IsGreater.greaterThan;


/**
 * Date: 7/29/2014.
 *
 * @author lesant
 */
public class MatcherFactory {
	public static <T extends Comparable> Matcher<T> getMatcher(MatchType matchType, T compareArg){
		Validate.notNull(matchType, "Match type cannot be null.");
		Matcher<T> matcher = anything();
		switch (matchType) {

			case EQUAL:
				matcher = equalTo(compareArg);
				break;
			case COMPARE_GREATER:
				matcher = greaterThan(compareArg);
				break;
			case NOT_EQUAL:
				matcher = not(compareArg);
				break;
			case DEFAULT:
				break;

		}

		return matcher;
	}
}
