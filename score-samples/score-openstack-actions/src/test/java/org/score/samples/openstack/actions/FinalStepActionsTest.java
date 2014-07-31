package org.score.samples.openstack.actions;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class FinalStepActionsTest {
	private static final long DEFAULT_TIMEOUT = 5000;

	@Test (timeout = DEFAULT_TIMEOUT)
	public void testSuccessStepAction() throws Exception {
		FinalStepActions finalStepActions = new FinalStepActions();
		Map<String, String> actionResult = finalStepActions.successStepAction();
		assertNotNull("Return map should not be null", actionResult);
		assertEquals("Return map should be empty", true, actionResult.isEmpty());
	}
}