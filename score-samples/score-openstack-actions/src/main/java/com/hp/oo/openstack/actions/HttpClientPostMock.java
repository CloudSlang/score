package com.hp.oo.openstack.actions;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 7/14/2014
 *
 * @author Bonczidai Levente
 */
public class HttpClientPostMock {
    private final static Logger logger = Logger.getLogger(HttpClientPostMock.class);

    public HttpClientPostMock() {
    }

	/**
	 * Mocked action method for Http Client Post Raw action
	 *
	 * @param username login username
	 * @param password login password
	 * @param url url of the request
	 * @param host host of the request
	 * @return map of action results (in current implementation contains the arguments passed to the method)
	 */
    public Map<String, String> post(String username, String password, String url, String host) {
        Map<String, String> returnMap = new HashMap<>();

        logger.info("username=" + username);
        logger.info("password=" + password);
        logger.info("uri=" + url);
		logger.info("host=" + host);

		returnMap.put("username", username);
        returnMap.put("password", password);
        returnMap.put("url", url);
		returnMap.put("host", host);

		returnMap.put("nextStep", "1");

        return returnMap;
    }
}
