package com.hp.oo.openstack.actions;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 7/21/2014
 *
 * @author Bonczidai Levente
 */
@SuppressWarnings("unused")
public class HttpClientSendEmailMock {
	private final static Logger logger = Logger.getLogger(HttpClientSendEmailMock.class);

	/**
	 * Mocked action method for sending an email
	 *
	 * @param receiver receiver of the email
	 * @param title title of the email
	 * @param body body if the email
	 * @return map of action results
	 */
	public Map<String, String> sendEmail(String receiver, String title, String body) {

		Map<String, String> returnMap =  new HashMap<String, String>();
		logger.info("receiver=" + receiver);
		logger.info("title=" + title);
		logger.info("body=" + body);

		returnMap.put("receiver", receiver);
		returnMap.put("title", title);
		returnMap.put("body", body);

		returnMap.put("nextStep", "2");
		return returnMap;
	}
}
