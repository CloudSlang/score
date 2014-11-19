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
