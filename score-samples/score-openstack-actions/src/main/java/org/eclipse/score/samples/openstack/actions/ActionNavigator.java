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

import org.eclipse.score.events.ScoreEvent;
import org.eclipse.score.lang.ExecutionRuntimeServices;
import org.apache.log4j.Logger;
import org.hamcrest.Matcher;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;

/**
 * Date: 7/31/2014
 *
 * @author lesant
 */
@SuppressWarnings("unused")
public class ActionNavigator {
	private final static Logger logger = Logger.getLogger(ActionNavigator.class);
	public final static String FAILURE_EVENT_KEY = "failureEvent";
	public <T extends Comparable> Long navigateWithMatchers(Map<String, Serializable> executionContext, List<NavigationMatcher<Serializable>> navigationMatchers, ExecutionRuntimeServices executionRuntimeServices) {
		logger.info("navigateWithMatchers method invocation");
		ArrayDeque<ScoreEvent> events = executionRuntimeServices.getEvents();
		if (events != null) {
			for (ScoreEvent scoreEvent : events) {
				if (scoreEvent.getEventType().equals(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE)) {
					return null;
				}
			}
		}

		if (navigationMatchers == null) {
			return null;
		}

		for (NavigationMatcher navigationMatcher : navigationMatchers) {
			Serializable response = executionContext.get(navigationMatcher.getContextKey());
			Matcher matcher = MatcherFactory.getMatcher(navigationMatcher.getMatchType(), (Comparable) navigationMatcher.getCompareArg());
			if (matcher.matches(response)) {
				return navigationMatcher.getNextStepId();
			}
		}

		return null;

	}

}
