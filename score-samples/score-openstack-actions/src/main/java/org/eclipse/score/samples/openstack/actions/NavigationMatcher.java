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
