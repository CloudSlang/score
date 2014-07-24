package com.hp.oo.openstack.actions;

import org.junit.Test;

public class HttpClientPostExecutionPlanTest {
	private HttpClientPostExecutionPlan httpClientPostExecutionPlan = new HttpClientPostExecutionPlan();

	@Test
	public void testCreateExecutionPlan() throws Exception {
		httpClientPostExecutionPlan.createExecutionPlan();
	}
}