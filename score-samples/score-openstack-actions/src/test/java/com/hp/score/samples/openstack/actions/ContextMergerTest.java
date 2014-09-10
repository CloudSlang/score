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
