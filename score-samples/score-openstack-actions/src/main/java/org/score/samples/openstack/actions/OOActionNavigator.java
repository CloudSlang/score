package org.score.samples.openstack.actions;

import com.hp.score.events.ScoreEvent;
import com.hp.score.lang.ExecutionRuntimeServices;
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
public class OOActionNavigator {
	private final static Logger logger = Logger.getLogger(OOActionNavigator.class);

	public <T> Long navigate (Map<String, Serializable> executionContext, List<NavigationMatcher<Serializable>> navigationMatchers, ExecutionRuntimeServices executionRuntimeServices) {
		logger.info("navigate method invocation");

		//check if an exception occurred in run method through events
		ArrayDeque<ScoreEvent> events = executionRuntimeServices.getEvents();
		for (ScoreEvent scoreEvent : events) {
			if (scoreEvent.getEventType().equals(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE)) {
				return null;
			}
		}

		if (navigationMatchers == null) {
			return null;
		}

		for(NavigationMatcher navigationMatcher : navigationMatchers)
		{
			Serializable response = executionContext.get(navigationMatcher.getContextKey());
			Matcher matcher = MatcherFactory.getMatcher(navigationMatcher.getMatchType(), navigationMatcher.getCompareArg());
			if(matcher.matches(response)){
				return navigationMatcher.getNextStepId();
			}
		}

		return null;
	}
}
