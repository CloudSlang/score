package com.hp.oo.openstack.actions;

import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class HttpClientPostWrapperTest {
	private static final String USERNAME_KEY = "USERNAME";
	private static final String PASSWORD_KEY = "PASSWORD";
	private static final String URL_KEY = "URL";
	private static final String HOST_KEY = "HOST";
	private static final String USERNAME = "testUser";
	private static final String PASSWORD = "testPass";
	private static final String URL = "testUrl";
	private static final String HOST = "tesHost";
	private static final long DEFAULT_TIMEOUT = 1000;

	private final HttpClientPostMock httpClientPostMock = mock(HttpClientPostMock.class);
	private HttpClientPostWrapper postWrapper = new HttpClientPostWrapper(httpClientPostMock);
	private Map<String, Serializable> executionContext;

	@Before
	public void setUp() throws Exception {
		reset(httpClientPostMock);
	}

	/**
	 *  executionContext = empty, parameters = null;
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test (timeout = DEFAULT_TIMEOUT)
	public void testPostCase1() throws Exception {
		//httpClientPostMock
		when(httpClientPostMock.post(eq(USERNAME), eq(PASSWORD), eq(URL), eq(HOST))).
																							thenReturn(createMapForMock(USERNAME, PASSWORD, URL));

		executionContext = new HashMap<>();
		postWrapper.post(USERNAME, PASSWORD, URL, HOST, executionContext);

		verify(httpClientPostMock).post(eq(USERNAME), eq(PASSWORD), eq(URL), eq(HOST));

		//executionContext should contain only 3 pairs
		assertEquals(executionContext.size(), 3);

		//contains the post parameters
		verifyBaseParameters();
	}

	//	/**
//	 * executionContext=contains some initial parameters (disjunction with the post return results), parameters = null;
//	 *
//	 * @throws Exception
//	 */
//	@SuppressWarnings("unchecked")
//	@Test (timeout = DEFAULT_TIMEOUT)
//	public void testPostCase2() throws Exception {
//		//httpClientPostMock
//		when(httpClientPostMock.post(eq(USERNAME), eq(PASSWORD), eq(URL), (Map<String, String>) eq(null))).
//			thenReturn(createMapForMock(USERNAME, PASSWORD, URL));
//
//		executionContext = new HashMap<>();
//		String parameter1 = "parameter1";
//		String parameter2 = "parameter2";
//		String value1 = "value1";
//		String value2 = "value2";
//		executionContext.put(parameter1, value1);
//		executionContext.put(parameter2, value2);
//
//		postWrapper.post(USERNAME, PASSWORD, URL, null, executionContext);
//
//		verify(httpClientPostMock).post(eq(USERNAME), eq(PASSWORD), eq(URL), (Map<String, String>) eq(null));
//
//		//executionContext should contain only 5 pairs
//		assertEquals(executionContext.size(), 5);
//
//		//contains the post parameters
//		verifyBaseParameters();
//
//		//initial parameters unchanged
//		assertTrue(executionContext.containsKey(parameter1));
//		String value1Map = (String) executionContext.get(parameter1);
//		assertEquals(value1, value1Map);
//
//		assertTrue(executionContext.containsKey(parameter2));
//		String value2Map = (String) executionContext.get(parameter2);
//		assertEquals(value2, value2Map);
//	}
//
//	/**
//	 * executionContext=empty, parameters = contains values;
//	 *
//	 * @throws Exception
//	 */
//	@Test (timeout = DEFAULT_TIMEOUT)
//	public void testPostCase3() throws Exception {
//		Map<String, String> parameters = new HashMap<>();
//		String parameter1 = "parameter1";
//		String parameter2 = "parameter2";
//		String value1 = "value1";
//		String value2 = "value2";
//		parameters.put(parameter1, value1);
//		parameters.put(parameter2, value2);
//		Map<String, String> mockReturnMap = new HashMap<>();
//		mockReturnMap.putAll(parameters);
//		mockReturnMap.putAll(createMapForMock(USERNAME, PASSWORD, URL));
//
//		//httpClientPostMock
//		when(httpClientPostMock.post(eq(USERNAME), eq(PASSWORD), eq(URL), eq(parameters))).thenReturn(mockReturnMap);
//
//		executionContext = new HashMap<>();
//		postWrapper.post(USERNAME, PASSWORD, URL, parameters, executionContext);
//
//		verify(httpClientPostMock).post(eq(USERNAME), eq(PASSWORD), eq(URL), eq(parameters));
//
//		//executionContext should contain only 5 pairs
//		assertEquals(executionContext.size(), 5);
//
//		//contains the post parameters
//		this.verifyBaseParameters();
//
//		//verify post parameters
//		assertTrue(executionContext.containsKey(parameter1));
//		String value1Map = (String) executionContext.get(parameter1);
//		assertEquals(value1, value1Map);
//
//		assertTrue(executionContext.containsKey(parameter2));
//		String value2Map = (String) executionContext.get(parameter2);
//		assertEquals(value2, value2Map);
//	}
//
//	/**
//	 * executionContext = contains some initial parameters (conjunction with the post return results)
//	 * the old parameters should be overridden
//	 * parameters = contains some values;
//	 *
//	 * @throws Exception
//	 */
//	@Test (timeout = DEFAULT_TIMEOUT)
//	public void testPostCase4() throws Exception {
//		//post parameters
//		Map<String, String> parameters = new HashMap<>();
//		String parameter1 = "parameter1";
//		String parameter2 = "parameter2";
//		String value1 = "value1";
//		String value2 = "value2";
//		parameters.put(parameter1, value1);
//		parameters.put(parameter2, value2);
//		Map<String, String> mockReturnMap = new HashMap<>();
//		mockReturnMap.putAll(parameters);
//		mockReturnMap.putAll(createMapForMock(USERNAME, PASSWORD, URL));
//
//		//httpClientPostMock
//		when(httpClientPostMock.post(eq(USERNAME), eq(PASSWORD), eq(URL), eq(parameters))).
//																					thenReturn(mockReturnMap);
//
//		executionContext = new HashMap<>();
//		String parameter1Old = "parameter1Old";
//		String parameter2Old = "parameter2"; //conflict here
//		String value1Old = "value1Old";
//		String value2Old = "value2"; //conflict here
//		executionContext.put(parameter1Old, value1Old);
//		executionContext.put(parameter2Old, value2Old);
//
//		postWrapper.post(USERNAME, PASSWORD, URL, parameters, executionContext);
//
//		verify(httpClientPostMock).post(eq(USERNAME), eq(PASSWORD), eq(URL), eq(parameters));
//
//		//executionContext should contain only 6 pairs
//		assertEquals(executionContext.size(), 6);
//
//		//contains the post parameters
//		verifyBaseParameters();
//
//		//initial parameter1 unchanged
//		assertTrue(executionContext.containsKey(parameter1Old));
//		String value1MapOld = (String) executionContext.get(parameter1Old);
//		assertEquals(value1Old, value1MapOld);
//
//		//the key with conflict should be overridden
//		assertTrue(executionContext.containsKey(parameter2Old));
//		String valConflict = (String) executionContext.get(parameter2Old);
//		assertEquals(value2, valConflict);
//	}
//
//	/**
//	 * Helper method for creating initial map
//	 *
//	 * @param username login username
//	 * @param password login password
//	 * @param url url of the request
//	 * @return map that contains the key-value pairs. Values are defined as method arguments arguments while
//	 * the corresponding keys are defined as constants
//	 */
	private Map<String, String> createMapForMock(String username, String password, String url) {
		Map<String, String> returnMap = new HashMap<>(3);
		returnMap.put(USERNAME_KEY, username);
		returnMap.put(PASSWORD_KEY, password);
		returnMap.put(URL_KEY, url);
		return returnMap;
	}

	/**
	 * verifies if the execution context contains the correct USERNAME, PASSWORD, URL parameters
	 */
	private void verifyBaseParameters() {
		assertTrue(executionContext.containsKey(USERNAME_KEY));
		String usernameMap = (String) executionContext.get(USERNAME_KEY);
		assertEquals(USERNAME, usernameMap);

		assertTrue(executionContext.containsKey(PASSWORD_KEY));
		String passwordMap = (String) executionContext.get(PASSWORD_KEY);
		assertEquals(PASSWORD, passwordMap);

		assertTrue(executionContext.containsKey(URL_KEY));
		String urlMap = (String) executionContext.get(URL_KEY);
		assertEquals(URL, urlMap);
	}
//
//	/**
//	 * tests the post navigation action when return value should be Success
//	 *
//	 * @throws Exception
//	 */
//	@Test (timeout = DEFAULT_TIMEOUT)
//	public void testPostNavigationSuccess() throws Exception {
//		executionContext = new HashMap<>();
//		executionContext.put(HttpClientPostWrapper.RESPONSE, HttpClientPostWrapper.SUCCESS);
//
//		Long expected = HttpClientPostExecutionPlan.SUCCESS_STEP_ID;
//		Long actual = postWrapper.postNavigation(executionContext);
//		assertEquals(expected, actual);
//	}
//
//	/**
//	 * tests the post navigation action when return value should be Failure
//	 *
//	 * @throws Exception
//	 */
//	@Test (timeout = DEFAULT_TIMEOUT)
//	public void testPostNavigationFailure() throws Exception {
//		executionContext = new HashMap<>();
//		executionContext.put(HttpClientPostWrapper.RESPONSE, HttpClientPostWrapper.FAILURE);
//
//		Long expected = HttpClientPostExecutionPlan.FAILURE_STEP_ID;
//		Long actual = postWrapper.postNavigation(executionContext);
//		assertEquals(expected, actual);
//	}
}
