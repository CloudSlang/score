package org.score.samples.openstack.actions;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;
import org.junit.Test;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OOActionRunnerTest {
	private static final String RESPONSE_KEY = "response";
	private static final String SUCCESS = "success";

	public static final String ACTION_PARAMETER_1_KEY = "actionParameter1";
	public static final String ACTION_PARAMETER_2_KEY = "actionParameter2";
	public static final String ACTION_PARAMETER_3_KEY = "actionParameter3";
	private static final String ACTION_PARAMETER1_VALUE = "methodParameter1Value";
	private static final String ACTION_PARAMETER2_VALUE = "methodParameter2Value";
	private static final String ACTION_PARAMETER3_VALUE = "methodParameter3Value";

	private static final String INITIAL_CONTEXT_KEY1 = ACTION_PARAMETER_1_KEY; //override occurs here
	private static final String INITIAL_CONTEXT_KEY2 = "ExecutionContextKey2";
	private static final String INITIAL_CONTEXT_VALUE1 = "ExecutionContextValue1";
	private static final String INITIAL_CONTEXT_VALUE2 = "ExecutionContextValue2";

	private static final long DEFAULT_TIMEOUT = 5000;

	@Test (timeout = DEFAULT_TIMEOUT)
	public void testRunWithoutExceptions() throws Exception {
		ExecutionRuntimeServices executionRuntimeServicesMock = mock(ExecutionRuntimeServices.class);

		Map<String, Serializable> actualExecutionContext = prepareActualExecutionContext();
		Map<String, Serializable> expectedExecutionContext = prepareExpectedExecutionContext(actualExecutionContext);

		runAction(actualExecutionContext, new HashMap<String, Object>(), executionRuntimeServicesMock, "org.score.samples.openstack.actions.OOActionRunnerTest", "auxiliaryAction");

		//verify if method adds the action runtime events
		verify(executionRuntimeServicesMock, times(4))
				.addEvent(eq(OOActionRunner.ACTION_RUNTIME_EVENT_TYPE), any(String.class));

		testExecutionContext(expectedExecutionContext, actualExecutionContext);
	}

	@Test (timeout = DEFAULT_TIMEOUT)
	public void testRunWithClassNotFoundException() throws Exception {
		ExecutionRuntimeServices executionRuntimeServicesMock = mock(ExecutionRuntimeServices.class);
		Map<String, Serializable> actualExecutionContext = prepareActualExecutionContext();

		runAction(actualExecutionContext, new HashMap<String, Object>(), executionRuntimeServicesMock, "org.score.samples.openstack.actions.IDontExist", "auxiliaryAction");

		//verify if method adds the exception event
		verify(executionRuntimeServicesMock)
				.addEvent(eq(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE), any(String.class));
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

	private void runAction(
			Map<String, Serializable> executionContext,
            Map<String, Object> nonSerializableExecutionData,
            ExecutionRuntimeServices executionRuntimeServices,
			String actionClassName,
			String actionMethodName)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
		OOActionRunner runner = new OOActionRunner();
		runner.run(executionContext, nonSerializableExecutionData, executionRuntimeServices, actionClassName, actionMethodName, new ArrayList<InputBinding>());
	}

	private Map<String, Serializable> prepareActualExecutionContext() {
		Map<String, Serializable> executionContext = new HashMap<>();
		executionContext.put(INITIAL_CONTEXT_KEY1, INITIAL_CONTEXT_VALUE1);
		executionContext.put(INITIAL_CONTEXT_KEY2, INITIAL_CONTEXT_VALUE2);

		executionContext.put("methodParameter1", ACTION_PARAMETER1_VALUE);
		executionContext.put("methodParameter2", ACTION_PARAMETER2_VALUE);
		executionContext.put("methodParameter3", ACTION_PARAMETER3_VALUE);

		return executionContext;
	}

	@SuppressWarnings("unused")
	public Map<String, String> auxiliaryAction(@Param("methodParameter1") String methodParameter1,
                                               @Param("methodParameter2") String methodParameter2,
                                               @Param("methodParameter3") String methodParameter3) {
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