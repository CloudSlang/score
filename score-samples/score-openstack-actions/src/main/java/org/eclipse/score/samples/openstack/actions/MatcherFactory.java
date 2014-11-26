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

import org.apache.commons.lang.Validate;
import org.hamcrest.Matcher;

import static org.eclipse.score.samples.openstack.actions.IsGreaterOrEqual.greaterOrEqualTo;
import static org.eclipse.score.samples.openstack.actions.IsLess.lessThan;
import static org.eclipse.score.samples.openstack.actions.IsLessOrEqual.lessOrEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.anything;
import static org.eclipse.score.samples.openstack.actions.IsGreater.greaterThan;


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
			case COMPARE_GREATER_OR_EQUAL:
				matcher = greaterOrEqualTo(compareArg);
				break;
			case COMPARE_LESS:
				matcher = lessThan(compareArg);
				break;
			case COMPARE_LESS_OR_EQUAL:
				matcher = lessOrEqualTo(compareArg);
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
