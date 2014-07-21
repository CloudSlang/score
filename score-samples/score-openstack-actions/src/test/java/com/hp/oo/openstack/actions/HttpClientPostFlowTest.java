package com.hp.oo.openstack.actions;

import org.junit.Test;

public class HttpClientPostFlowTest {
	private HttpClientPostFlow httpClientPostFlow = new HttpClientPostFlow();

	@Test
	public void testCreateExecutionPlan() throws Exception {
		httpClientPostFlow.createExecutionPlan();
	}
}