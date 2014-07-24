package com.hp.oo.openstack.actions;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 7/18/2014
 *
 * @author Bonczidai Levente
 */
public class ReturnStepActions {
	public Map<String, String> successStepAction() {
		Map<String, String> returnMap = new HashMap<String, String>();
		returnMap.put("nextStep", "null");
		return returnMap;
	}
}
