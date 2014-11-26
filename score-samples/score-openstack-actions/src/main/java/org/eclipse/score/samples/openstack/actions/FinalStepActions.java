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
