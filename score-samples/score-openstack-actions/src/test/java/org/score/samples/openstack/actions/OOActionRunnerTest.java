package org.score.samples.openstack.actions;

import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

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

	private static final long DEFAULT_TIMEOUT = 1000;

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testRun() throws Exception {
		Map<String, Serializable> actualExecutionContext = prepareActualExecutionContext();
		Map<String, Serializable> expectedExecutionContext = prepareExpectedExecutionContext(actualExecutionContext);

		runAction(actualExecutionContext);

		testExecutionContext(expectedExecutionContext, actualExecutionContext);
	}

	private Map<String, Serializable> prepareExpectedExecutionContext(Map<String, Serializable> initialMap) {
		Map<String, Serializable> expectedExecutionContext = new HashMap<>();
		expectedExecutionContext.putAll(initialMap);

		Map<String, String> auxiliaryMap = getAuxiliaryMap(ACTION_PARAMETER1_VALUE, ACTION_PARAMETER2_VALUE, ACTION_PARAMETER3_VALUE);
		expectedExecutionContext.putAll(auxiliaryMap);

		return expectedExecutionContext;
	}

	private void testExecutionContext(Map<String, Serializable> expectedExecutionContext,
									  Map<String, Serializable> actualExecutionContext) {
		assertEquals("execution contexts should be equal", expectedExecutionContext, actualExecutionContext);
	}

	private void runAction(Map<String, Serializable> executionContext) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
		OOActionRunner runner = new OOActionRunner();
		runner.run(executionContext, "org.score.samples.openstack.actions.OOActionRunnerTest", "auxiliaryAction");
	}

	private Map<String, Serializable> prepareActualExecutionContext() {
		Map<String, Serializable> executionContext = new HashMap<>();
		executionContext.put(PARAMETER1_CONTEXT_KEY, PARAMETER1_CONTEXT_VALUE);
		executionContext.put(PARAMETER2_CONTEXT_KEY, PARAMETER2_CONTEXT_VALUE);

		executionContext.put("methodParameter1", ACTION_PARAMETER1_VALUE);
		executionContext.put("methodParameter2", ACTION_PARAMETER2_VALUE);
		executionContext.put("methodParameter3", ACTION_PARAMETER3_VALUE);

		return executionContext;
	}

	@SuppressWarnings("unused")
	public Map<String, String> auxiliaryAction(String methodParameter1,
											   String methodParameter2,
											   String methodParameter3) {
		return getAuxiliaryMap(methodParameter1, methodParameter2, methodParameter3);
	}

	private Map<String, String> getAuxiliaryMap(String methodParameter1, String methodParameter2, String methodParameter3) {
		Map<String, String> returnMap = new HashMap<>();
		returnMap.put(ACTION_PARAMETER_1_KEY, methodParameter1);
		returnMap.put(ACTION_PARAMETER_2_KEY, methodParameter2);
		returnMap.put(ACTION_PARAMETER_3_KEY, methodParameter3);
		returnMap.put(RESPONSE_KEY, SUCCESS);
		return returnMap;
	}

}