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
package com.hp.score.samples.openstack.actions;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 7/18/2014
 *
 * @author Bonczidai Levente
 */
public class FinalStepActions {
	private final static Logger logger = Logger.getLogger(FinalStepActions.class);
	public static final String RESPONSE_KEY = "response";
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";

	public Map<String, String> successStepAction() {
		logger.info("This is a success step action");
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put(RESPONSE_KEY, SUCCESS);
		return returnMap;
	}

	public Map<String, String> failureStepAction() {
		logger.info("This is a failure step action");
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put(RESPONSE_KEY, FAILURE);
		return returnMap;
	}
}
