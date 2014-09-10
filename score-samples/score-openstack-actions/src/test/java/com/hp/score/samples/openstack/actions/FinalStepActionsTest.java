package com.hp.score.samples.openstack.actions;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static com.hp.score.samples.openstack.actions.FinalStepActions.*;

public class FinalStepActionsTest {
	private static final long DEFAULT_TIMEOUT = 20000;

	@Test (timeout = DEFAULT_TIMEOUT)
	public void testSuccessStepAction() throws Exception {
		FinalStepActions finalStepActions = new FinalStepActions();
		Map<String, String> actionResult = finalStepActions.successStepAction();
		assertNotNull("Return map should not be null", actionResult);
		String response = actionResult.get(RESPONSE_KEY);
		assertEquals("Response should be success", SUCCESS, response);
	}

	@Test (timeout = DEFAULT_TIMEOUT)
	public void testFailureStepAction() throws Exception {
		FinalStepActions finalStepActions = new FinalStepActions();
		Map<String, String> actionResult = finalStepActions.failureStepAction();
		assertNotNull("Return map should not be null", actionResult);
		String response = actionResult.get(RESPONSE_KEY);
		assertEquals("Response should be failure", FAILURE, response);
	}
}