package com.hp.oo.openstack.actions;

import java.io.Serializable;
import java.util.Map;

public class HttpClientPostWrapper {
	public static final String RESPONSE = "response";
	public static final String SUCCESS = "success";
	public static final String FAILURE = "failure";

	private HttpClientPostMock httpClientPostMock;

	@SuppressWarnings("unused")
	public HttpClientPostWrapper() {
		this.httpClientPostMock = new HttpClientPostMock();
	}

	public HttpClientPostWrapper(HttpClientPostMock httpClientPostMock) {
		this.httpClientPostMock = httpClientPostMock;
	}

	/**
	 * Control action that wraps around the Http Client Post Raw action
	 *
	 * @param username login username
	 * @param password login password
	 * @param url url of the request
	 * @param host host of the request
	 * @param executionContext the current execution context
	 */
	public void post(String username,
					 String password,
					 String url,
					 String host,
					 Map<String, Serializable> executionContext) {



		// invoke “HTTP Client post raw”  action
		// returnMap contains the results of the action
		Map<String, String> returnMap = httpClientPostMock.post(username, password, url, host);

		//merge back the results of the action in the flow execution context
		if (executionContext != null && returnMap != null) {
			executionContext.putAll(returnMap);
		}
	}

	public Long postNavigation(Map<String, Serializable> executionContext) {
		if (executionContext.containsKey(RESPONSE)) {
			Serializable rawResult = executionContext.get(RESPONSE);
			if (rawResult instanceof String) {
				return (SUCCESS.equals(rawResult)) ? HttpClientPostExecutionPlan.SUCCESS_STEP_ID : HttpClientPostExecutionPlan.FAILURE_STEP_ID;
			}
		}
		return HttpClientPostExecutionPlan.FAILURE_STEP_ID;
	}
}
