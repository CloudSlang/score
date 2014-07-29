package org.score.samples.openstack.actions;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 7/18/2014
 *
 * @author Bonczidai Levente
 */
public class ReturnStepActions {
	private final static Logger logger = Logger.getLogger(ReturnStepActions.class);

	@SuppressWarnings("unused") //todo test when method will be finished
	public Map<String, String> successStepAction() {
		logger.info("This is a return step action");

		//Map<String, String> returnMap = new HashMap<>();
		//returnMap.put("nextStep", "null");
		return new HashMap<>();
	}
}
