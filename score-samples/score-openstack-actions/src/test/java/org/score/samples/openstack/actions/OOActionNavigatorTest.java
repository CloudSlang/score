package org.score.samples.openstack.actions;

import com.hp.score.events.ScoreEvent;
import com.hp.score.lang.ExecutionRuntimeServices;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Date: 7/31/2014
 *
 * @author lesant
 */

public class OOActionNavigatorTest {
	public static final String ACTION_PARAMETER_1_KEY = "actionParameter1";
	private static final String ACTION_PARAMETER1_VALUE = "methodParameter1Value";
	private static final String ACTION_PARAMETER2_VALUE = "methodParameter2Value";
	private static final String ACTION_PARAMETER3_VALUE = "methodParameter3Value";

	private static final String INITIAL_CONTEXT_KEY1 = ACTION_PARAMETER_1_KEY; //override occurs here
	private static final String INITIAL_CONTEXT_KEY2 = "ExecutionContextKey2";
	private static final String INITIAL_CONTEXT_VALUE1 = "ExecutionContextValue1";
	private static final String INITIAL_CONTEXT_VALUE2 = "ExecutionContextValue2";

	private static final String RESPONSE_KEY = "response";
	private static final String SUCCESS = "success";

	private static final long DEFAULT_TIMEOUT = 5000;

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testNavigate(){
		ExecutionRuntimeServices executionRuntimeServicesMock = mock(ExecutionRuntimeServices.class);
		ArrayDeque<ScoreEvent> events = new ArrayDeque<>();

		when(executionRuntimeServicesMock.getEvents()).thenReturn(events);
		Map<String, Serializable> actualExecutionContext = prepareActualExecutionContext();


		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();

		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "response", "success", 1L));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.NOT_EQUAL, "response", "success", 2L));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 2L));

		OOActionNavigator navigator = new OOActionNavigator();
		Long result = navigator.navigate(actualExecutionContext, navigationMatchers, executionRuntimeServicesMock);
		assertEquals("navigate() should return 1L",(Object) result, 1L);

		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.NOT_EQUAL, "response", "fail", 2L));
		result = navigator.navigate(actualExecutionContext, navigationMatchers, executionRuntimeServicesMock);
		assertEquals("navigate() should return 2L",(Object) result, 2L);

		verify(executionRuntimeServicesMock, times(2)).getEvents();

		navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, "response", "fail", 3L));
		result = navigator.navigate(actualExecutionContext, navigationMatchers, executionRuntimeServicesMock);
		assertEquals("navigate() should return 3L",(Object) result, 3L);

		verify(executionRuntimeServicesMock, times(3)).getEvents();

	}
	@Test(timeout = DEFAULT_TIMEOUT)
	public void testNavigateWithException(){
		ExecutionRuntimeServices executionRuntimeServicesMock = mock(ExecutionRuntimeServices.class);
		ArrayDeque<ScoreEvent> events = new ArrayDeque<>();
		events.add(new ScoreEvent(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE, ""));

		when(executionRuntimeServicesMock.getEvents()).thenReturn(events);
		Map<String, Serializable> actualExecutionContext = prepareActualExecutionContext();

		List<NavigationMatcher<Serializable>> navigationMatchers = new ArrayList<>();
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.EQUAL, "result", "200", 1L));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.NOT_EQUAL, "result", "200", 1L));
		navigationMatchers.add(new NavigationMatcher<Serializable>(MatchType.DEFAULT, 2L));

		OOActionNavigator navigator = new OOActionNavigator();
		assertEquals("navigate() should return null", navigator.navigate(actualExecutionContext, navigationMatchers, executionRuntimeServicesMock), null);

		verify(executionRuntimeServicesMock).getEvents();

	}

	@Test(timeout = DEFAULT_TIMEOUT)
	public void testNavigateWithoutNavigationMatchers(){
		ExecutionRuntimeServices executionRuntimeServicesMock = mock(ExecutionRuntimeServices.class);
		ArrayDeque<ScoreEvent> events = new ArrayDeque<>();
		events.add(new ScoreEvent(OOActionRunner.ACTION_EXCEPTION_EVENT_TYPE, ""));

		when(executionRuntimeServicesMock.getEvents()).thenReturn(events);
		Map<String, Serializable> actualExecutionContext = prepareActualExecutionContext();

		OOActionNavigator navigator = new OOActionNavigator();
		assertEquals("navigate() should return null", navigator.navigate(actualExecutionContext, null, executionRuntimeServicesMock), null);

		verify(executionRuntimeServicesMock).getEvents();

	}

	@SuppressWarnings("unused")
	public void navigateToAction(Map<String, Serializable> executionContext, List<NavigationMatcher<Serializable>> navigationMatchers, ExecutionRuntimeServices executionRuntimeServices){

	}

	private Map<String, Serializable> prepareActualExecutionContext() {
		Map<String, Serializable> executionContext = new HashMap<>();
		executionContext.put(INITIAL_CONTEXT_KEY1, INITIAL_CONTEXT_VALUE1);
		executionContext.put(INITIAL_CONTEXT_KEY2, INITIAL_CONTEXT_VALUE2);

		executionContext.put("methodParameter1", ACTION_PARAMETER1_VALUE);
		executionContext.put("methodParameter2", ACTION_PARAMETER2_VALUE);
		executionContext.put("methodParameter3", ACTION_PARAMETER3_VALUE);
		executionContext.put(RESPONSE_KEY, SUCCESS);
		return executionContext;
	}




}
