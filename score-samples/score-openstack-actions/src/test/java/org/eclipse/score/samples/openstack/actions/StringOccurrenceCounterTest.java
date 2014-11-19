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
package org.eclipse.score.samples.openstack.actions;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Date: 8/20/2014
 *
 * @author lesant
 */
public class StringOccurrenceCounterTest {
	private static final long DEFAULT_TIMEOUT = 5000;

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testExecuteWithMissingInputs(){

		StringOccurrenceCounter stringOccurrenceCounter = new StringOccurrenceCounter();
		Map<String, String> returnResult = stringOccurrenceCounter.execute(null, "string", "true");
		assertEquals("Return code not as expected", "1", returnResult.get("returnCode"));

		returnResult = stringOccurrenceCounter.execute("string1, string2", null, "true");
		assertEquals("Return code not as expected", "1", returnResult.get("returnCode"));

		returnResult = stringOccurrenceCounter.execute("string1, string2", "string1", null);
		assertEquals("Return code not as expected", "1", returnResult.get("returnCode"));

	}
	@Test(timeout = DEFAULT_TIMEOUT)
	public void testExecute(){

		StringOccurrenceCounter stringOccurrenceCounter = new StringOccurrenceCounter();
		Map<String, String> returnResult = stringOccurrenceCounter.execute("string1, string2", "string1", "false");
		assertEquals("Return code not as expected", "0", returnResult.get("returnCode"));
		assertEquals("Result not as expected", "1", returnResult.get("returnResult"));

		returnResult = stringOccurrenceCounter.execute("string2, string2", "string2", "false");
		assertEquals("Result not as expected", "2", returnResult.get("returnResult"));

		returnResult = stringOccurrenceCounter.execute("string2, string2", "String2", "true");
		assertEquals("Result not as expected", "2", returnResult.get("returnResult"));
		returnResult = stringOccurrenceCounter.execute("string2, string2", "String2", "false");
		assertEquals("Result not as expected", "0", returnResult.get("returnResult"));

	}
}
