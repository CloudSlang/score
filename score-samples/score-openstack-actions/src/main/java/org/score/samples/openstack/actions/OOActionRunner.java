package org.score.samples.openstack.actions;

import com.hp.score.lang.ExecutionRuntimeServices;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 7/22/2014
 *
 * @author Bonczidai Levente
 */
public class OOActionRunner {
	private final static Logger logger = Logger.getLogger(OOActionRunner.class);
	public final static String ACTION_RUNTIME_EVENT_TYPE = "ACTION_RUNTIME_EVENT";
	public final static String ACTION_EXCEPTION_EVENT_TYPE = "ACTION_EXCEPTION_EVENT";
	public final static String FAILURE_EVENT_KEY = "failureEvent";
	private Class actionClass;
	private Method actionMethod;
	private String[] parameterNames; // parameter names ordered according to their position in method signature
	private Map<String, Object> parameters; //parameters as name-value pairs

	/**
	 * Wrapper method for running actions. A method is a valid action if it returns a Map<String, String>
	 * and its parameters are serializable.
	 *
	 * @param executionContext executionContext object populated by score
	 * @param executionRuntimeServices executionRuntimeServices object populated by score
	 * @param className full path of the actual action class
	 * @param methodName method name of the actual action
	 */
	public void run(
			Map<String, Serializable> executionContext,
			ExecutionRuntimeServices executionRuntimeServices,
			String className,
			String methodName,
			List<InputBinding> inputBindings) {
		try {
			logger.info("run method invocation");

			Object[] actualParameters = extractMethodData(executionContext, executionRuntimeServices, className, methodName);

			verifyActionInputs(inputBindings);

			Map<String, String> results = invokeMethod(executionRuntimeServices, className, methodName, actualParameters);
			// look for exception
			mergeBackResults(executionContext, executionRuntimeServices, methodName, results);
		} catch (Exception ex) {
			executionRuntimeServices.addEvent(ACTION_EXCEPTION_EVENT_TYPE, ex);
			executionContext.put(FAILURE_EVENT_KEY, ex.getStackTrace());

		}
	}

	private void verifyActionInputs(List<InputBinding> inputBindings) throws IOException, InputBinding.InputBindingException {
		if (inputBindings != null) {
			for (InputBinding inputBinding : inputBindings) {
				if (inputBinding.isRequired()) {
					if (!foundValue(inputBinding.getInputName())) {
						String message = "Input \"" + inputBinding.getInputName() + "\" is required but not found!";
						throw new InputBinding.InputBindingException(message);
					}
				}
			}
		}
	}

	private boolean foundValue(String key) {
		Object value = parameters.get(key);
		if (value instanceof String) {
			return !((String) value).isEmpty();
		} else {
			return value != null;
		}
	}

	private void mergeBackResults(Map<String, Serializable> executionContext, ExecutionRuntimeServices executionRuntimeServices, String methodName, Map<String, String> results) {

		for (String key : results.keySet()) {
			if (key.equals("exception")) {
				results.put("exception", StringUtils.substring(results.get("exception"), 0, 50));
			}
		}
		String resultString = results != null ? results.toString() : "";

		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Method \"" + methodName + "\" invoked.." +
				" Attempting to merge back results: " + resultString);

		//merge back the results of the action in the flow execution context
		doMerge(executionContext, results);
		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Results merged back in the Execution Context");
	}

	private Map<String, String> invokeMethod(ExecutionRuntimeServices executionRuntimeServices, String className, String methodName, Object[] actualParameters) throws Exception {
		String invokeMessage = "Attempting to invoke action method \"" + methodName + "\"";
		invokeMessage += " of class " + className;

		// if the action method does not have any parameters then actualParameters is null
		if (actualParameters != null) {
			invokeMessage += " with parameters: [";
			int lastElementIndex = actualParameters.length - 1;
			for (int i = 0; i < lastElementIndex; i++) {
				String parameter = actualParameters[i] == null ? "null" : actualParameters[i].toString();
				String pair = parameterNames[i] + " -> " + parameter;
				invokeMessage += pair + ",";
			}
			invokeMessage += parameterNames[lastElementIndex] + " -> " + actualParameters[lastElementIndex];
		}
		invokeMessage += "]";

		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, invokeMessage);

		// invoke action method
		return invokeActionMethod(actionMethod, actionClass.newInstance(), actualParameters);
	}

	private Object[] extractMethodData(Map<String, Serializable> executionContext, ExecutionRuntimeServices executionRuntimeServices, String className, String methodName) throws ClassNotFoundException {
		executionRuntimeServices.addEvent(ACTION_RUNTIME_EVENT_TYPE, "Extracting action data");

		//get the action class
		actionClass = Class.forName(className);

		//get the Method object
		actionMethod = getMethodByName(actionClass, methodName);

		//get the parameter names of the action method
		ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
		parameterNames = parameterNameDiscoverer.getParameterNames(actionMethod);

		//extract the parameters from execution context
		return getParametersFromExecutionContext(executionContext, parameterNames);
	}

	/**
	 * Extracts the actual method of the action's class
	 *
	 * @param actionClass Class object that represents the actual action class
	 * @param methodName  method name of the actual action
	 * @return actual method represented by Method object
	 * @throws ClassNotFoundException
	 */
	private Method getMethodByName(Class actionClass, String methodName) throws ClassNotFoundException {
		Method[] methods = actionClass.getDeclaredMethods();
		Method actionMethod = null;
		for (Method m : methods) {
			if (m.getName().equals(methodName)) {
				actionMethod = m;
			}
		}
		return actionMethod;
	}

	/**
	 * Retrieves a list of parameters from the execution context
	 *
	 * @param executionContext current Execution Context
	 * @param parameterNames   list of parameter names to be retrieved
	 * @return parameters from the execution context represented as Object list
	 */
	private Object[] getParametersFromExecutionContext(Map<String, Serializable> executionContext, String[] parameterNames) {
		parameters = new HashMap<>();
		int nrParameters = parameterNames.length;
		Object[] actualParameters = null;
		if (nrParameters > 0) {
			actualParameters = new Object[nrParameters];
			//actual parameter is passed from the execution context
			//if not present will be null
			for (int i = 0; i < nrParameters; i++) {
				if (executionContext.containsKey(parameterNames[i])) {
					actualParameters[i] = executionContext.get(parameterNames[i]);
				} else {
					actualParameters[i] = null;
				}
				parameters.put(parameterNames[i], actualParameters[i]);
			}
		}
		return actualParameters;
	}

	/**
	 * Invokes the actual action method with the specified parameters
	 *
	 * @param actionMethod action method represented as Method object
	 * @param instance     an instance of the invoker class
	 * @param parameters   method parameters
	 * @return results if the action
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 */
	@SuppressWarnings("unchecked")
	private Map<String, String> invokeActionMethod(Method actionMethod, Object instance, Object... parameters)
			throws Exception {
		Object returnObject = actionMethod.invoke(instance, parameters);
		Map<String, String> returnMap = (Map<String, String>) returnObject;
		if (returnMap == null) {
			throw new Exception("Action method did not return Map<String,String>");
		} else {
			return returnMap;
		}
	}

	/**
	 * Merges back the results in the execution context
	 *
	 * @param executionContext current Execution Context
	 * @param results          results to be merged back
	 */
	private void doMerge(Map<String, Serializable> executionContext, Map<String, String> results) {
		if (results != null) {
			executionContext.putAll(results);
		}
	}
}
