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

	public Map<String, String> successStepAction() {
		logger.info("This is a success step action");
		return new HashMap<>();
	}
}
