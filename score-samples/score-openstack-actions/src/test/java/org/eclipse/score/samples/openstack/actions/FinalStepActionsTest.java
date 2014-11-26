/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/
package org.eclipse.score.samples.openstack.actions;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.eclipse.score.samples.openstack.actions.FinalStepActions.*;

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