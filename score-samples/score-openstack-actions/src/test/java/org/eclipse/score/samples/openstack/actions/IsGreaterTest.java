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

import org.hamcrest.Matcher;
import org.junit.Test;
import static org.eclipse.score.samples.openstack.actions.IsGreater.greaterThan;

import static org.junit.Assert.assertEquals;

/**
 * Date: 8/27/2014.
 *
 * @author lesant
 */
public class IsGreaterTest {
	private static final long DEFAULT_TIMEOUT = 5000;
	private static final String RESULT_MESSAGE = "Result not as expected.";

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testGreaterThan(){

		Matcher matcher = greaterThan("20");

		assertEquals(RESULT_MESSAGE, false, matcher.matches("19"));
		assertEquals(RESULT_MESSAGE, true, matcher.matches("21"));
		assertEquals(RESULT_MESSAGE, false, matcher.matches("20"));
		assertEquals(RESULT_MESSAGE, true, matcher.matches("Hello"));
		assertEquals(RESULT_MESSAGE, false, matcher.matches("-1"));
		assertEquals(RESULT_MESSAGE, false, matcher.matches(null));
		matcher = greaterThan(null);
		assertEquals(RESULT_MESSAGE, true, matcher.matches("1"));
		assertEquals(RESULT_MESSAGE, false, matcher.matches(null));
	}

}
