package com.hp.oo.openstack.actions;

import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class OOActionRunnerTest {
	private static final String RESPONSE_KEY = "response";
	private static final String SUCCESS = "success";

	public static final String ACTION_PARAMETER_1_KEY = "actionParameter1";
	public static final String ACTION_PARAMETER_2_KEY = "actionParameter2";
	public static final String ACTION_PARAMETER_3_KEY = "actionParameter3";
	private static final String ACTION_PARAMETER1_VALUE = "methodPar1";
	private static final String ACTION_PARAMETER2_VALUE = "methodPar2";
	private static final String ACTION_PARAMETER3_VALUE = "methodPar3";

	private static final String PARAMETER1_CONTEXT_KEY = ACTION_PARAMETER_1_KEY;
	private static final String PARAMETER2_CONTEXT_KEY = "ExecutionContextKey2";
	private static final String PARAMETER1_CONTEXT_VALUE = "ExecutionContextValue1";
	private static final String PARAMETER2_CONTEXT_VALUE = "ExecutionContextValue2";

	@Test
	public void testRun() throws Exception {
		//initialize execution context
		Map<String, Serializable> executionContext = new HashMap<>();
		executionContext.put(PARAMETER1_CONTEXT_KEY, PARAMETER1_CONTEXT_VALUE);
		executionContext.put(PARAMETER2_CONTEXT_KEY, PARAMETER2_CONTEXT_VALUE);

		executionContext.put("methodParameter1", ACTION_PARAMETER1_VALUE);
		executionContext.put("methodParameter2", ACTION_PARAMETER2_VALUE);
		executionContext.put("methodParameter3", ACTION_PARAMETER3_VALUE);

		OOActionRunner runner = new OOActionRunner();
		runner.run(executionContext, "com.hp.oo.openstack.actions.OOActionRunnerTest", "auxiliaryAction");

		//Execution Context should contain the values from auxiliaryAction method
		assertEquals(8, executionContext.size());

		//initial parameter overridden when duplicate
		assertEquals(true, executionContext.containsKey(PARAMETER1_CONTEXT_KEY));
		assertEquals(ACTION_PARAMETER1_VALUE, executionContext.get(PARAMETER1_CONTEXT_KEY));

		//initial parameter unchanged when no duplicate
		assertEquals(true, executionContext.containsKey(PARAMETER2_CONTEXT_KEY));
		assertEquals(PARAMETER2_CONTEXT_VALUE, executionContext.get(PARAMETER2_CONTEXT_KEY));

		//Execution Context should contain the pairs returned by auxiliaryAction
		assertEquals(true, executionContext.containsKey(ACTION_PARAMETER_1_KEY));
		assertEquals(ACTION_PARAMETER1_VALUE, executionContext.get(ACTION_PARAMETER_1_KEY));
		assertEquals(true, executionContext.containsKey(ACTION_PARAMETER_2_KEY));
		assertEquals(ACTION_PARAMETER2_VALUE, executionContext.get(ACTION_PARAMETER_2_KEY));
		assertEquals(true, executionContext.containsKey(ACTION_PARAMETER_3_KEY));
		assertEquals(ACTION_PARAMETER3_VALUE, executionContext.get(ACTION_PARAMETER_3_KEY));

		assertEquals(true, executionContext.containsKey(RESPONSE_KEY));
		assertEquals(SUCCESS, executionContext.get(RESPONSE_KEY));
	}

	@SuppressWarnings("unused")
	public Map<String, String> auxiliaryAction(String methodParameter1,
											   String methodParameter2,
											   String methodParameter3) {
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put(ACTION_PARAMETER_1_KEY, methodParameter1);
		returnMap.put(ACTION_PARAMETER_2_KEY, methodParameter2);
		returnMap.put(ACTION_PARAMETER_3_KEY, methodParameter3);
		returnMap.put(RESPONSE_KEY, SUCCESS);
		return returnMap;
	}
}