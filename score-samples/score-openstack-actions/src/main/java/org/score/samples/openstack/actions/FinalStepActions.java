package org.score.samples.openstack.actions;

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
	public static final String SUCCESS_KEY = "success";
	public static final String FAILURE_KEY = "failure";

	public Map<String, String> successStepAction() {
		logger.info("This is a success step action");
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put(RESPONSE_KEY, SUCCESS_KEY);
		return returnMap;
	}

	public Map<String, String> failureStepAction() {
		logger.info("This is a failure step action");
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put(RESPONSE_KEY, FAILURE_KEY);
		return returnMap;
	}
}
