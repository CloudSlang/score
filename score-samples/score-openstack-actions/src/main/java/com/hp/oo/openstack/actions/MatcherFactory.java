package com.hp.oo.openstack.actions;

import org.hamcrest.Matcher;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.nullValue;


/**
 * Created by lesant on 7/29/2014.
 */
public class MatcherFactory {
	public Matcher getMatcher(MatchType matchType, String compareArg){
		Matcher matcher = null;
		switch (matchType) {

			case COMPARE_EQUAL:
				matcher = equalTo(Integer.parseInt(compareArg));
				break;
			case COMPARE_NOT_EQUAL:
				matcher = not(Integer.parseInt(compareArg));

			default:
				matcher = nullValue();
				break;
		}

		return matcher;
	}
}
