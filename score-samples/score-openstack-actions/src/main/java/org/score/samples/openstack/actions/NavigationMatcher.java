package org.score.samples.openstack.actions;

import java.io.Serializable;

/**
 * Created by lesant on 7/29/2014.
 */
public class NavigationMatcher implements Serializable {

	private MatchType matchType;
	private String nextStepId;
	private String compareArg;
	private String contextKey;

	public NavigationMatcher(MatchType matchType, String contextKey, String compareArg, String nextStepId) {
		this.matchType = matchType;
		this.nextStepId = nextStepId;
		this.contextKey = contextKey;
		this.compareArg = compareArg;
	}

	public NavigationMatcher() {
		this.matchType = null;
		this.nextStepId = "";
		this.contextKey = "";
	}
	public void setMatchType(MatchType matchType) {
		this.matchType = matchType;
	}

	public void setNextStepId(String nextStepId) {
		this.nextStepId = nextStepId;
	}

	public void setContextKey(String contextKey) {
		this.contextKey = contextKey;
	}

	public MatchType getMatchType() {
		return matchType;
	}

	public String getNextStepId() {
		return nextStepId;
	}

	public String getContextKey() {
		return contextKey;
	}
	public String getCompareArg() {
		return compareArg;
	}

	public void setCompareArg(String compareArg) {
		this.compareArg = compareArg;
	}





}
