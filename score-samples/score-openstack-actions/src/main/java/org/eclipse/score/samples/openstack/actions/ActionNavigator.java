/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
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
