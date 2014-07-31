package org.score.samples.openstack.actions;

import java.io.Serializable;

/**
 * Date: 7/29/2014.
 *
 * @author lesant
 */


public class NavigationMatcher<T> implements Serializable {

	private MatchType matchType;
	private String contextKey;
	private T compareArg;
	private Long nextStepId;

	@SuppressWarnings("unused")
	public NavigationMatcher() {
		this.matchType = null;
		this.nextStepId = 0L;
		this.contextKey = "";
	}
	public NavigationMatcher(MatchType matchType, Long nextStepId) {
		this.matchType = matchType;
		this.nextStepId = nextStepId;
	}
	public NavigationMatcher(MatchType matchType, String contextKey, T compareArg, Long nextStepId) {
		this.matchType = matchType;
		this.nextStepId = nextStepId;
		this.contextKey = contextKey;
		this.compareArg = compareArg;
	}

	public MatchType getMatchType() {
		return matchType;
	}

	public String getContextKey() {
		return contextKey;
	}

	public T getCompareArg() {
		return compareArg;
	}

	public Long getNextStepId() {
		return nextStepId;
	}

}
