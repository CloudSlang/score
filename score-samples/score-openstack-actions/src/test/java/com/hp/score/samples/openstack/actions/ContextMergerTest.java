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


import com.hp.score.lang.ExecutionRuntimeServices;
import org.junit.Test;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

import static org.mockito.Mockito.mock;


/**
 * Date: 8/11/2014
 *
 * @author lesant
 */

public class ContextMergerTest {

	private static final long DEFAULT_TIMEOUT = 5000;

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testMerge(){
		List<InputBinding> list = new ArrayList<>();

		list.add(InputBindingFactory.createMergeInputBindingWithSource("headers", "header"));
		list.add(InputBindingFactory.createMergeInputBindingWithValue("test","value"));
		ExecutionRuntimeServices executionRuntimeServicesMock = mock(ExecutionRuntimeServices.class);
		Map<String, Serializable> context = new HashMap<>();
		context.put("token", "hello");
		context.put("token2", "world");
		context.put("header", "X-AUTH-TOKEN: ${token}${token2}");
		ContextMerger merger = new ContextMerger();
		merger.merge(list, context, executionRuntimeServicesMock, "");
		assertEquals("Value not as expected.",  "X-AUTH-TOKEN: helloworld",  context.get("headers"));
		assertEquals("Value not as expected.",  "value",  context.get("test"));

	}

}
