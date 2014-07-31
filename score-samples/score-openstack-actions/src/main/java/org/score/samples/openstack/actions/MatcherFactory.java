package org.score.samples.openstack.actions;

import org.hamcrest.Matcher;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.nullValue;




/**
 * Created by lesant on 7/29/2014.
 */
public class MatcherFactory {
	public static <T> Matcher<T> getMatcher(MatchType matchType, T compareArg){
		Matcher<T> matcher;
		switch (matchType) {

			case EQUAL:
				matcher = equalTo(compareArg);
				break;
			case NOT_EQUAL:
				matcher = not(compareArg);
				break;
			case NONE:
				matcher = anything();
				break;
			default:
				matcher = nullValue();
				break;
		}

		return matcher;
	}
}
