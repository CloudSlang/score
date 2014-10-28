/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
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