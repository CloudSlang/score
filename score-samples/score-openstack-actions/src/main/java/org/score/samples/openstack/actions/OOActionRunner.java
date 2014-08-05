package org.score.samples.openstack.actions;

import com.hp.score.lang.ExecutionRuntimeServices;
import org.score.samples.utility.InputBindingUtility;
import org.score.samples.utility.InputBindingUtility.BindingConflict;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.List;
import java.util.Map;

/**
 * Date: 7/22/2014
 *
 * @author Bonczidai Levente
 */
public class OOActionRunner {
	private final static Logger logger = Logger.getLogger(OOActionRunner.class);
	public final static String ACTION_RUNTIME_EVENT_TYPE = "action_runtime_event";
	public final static String ACTION_EXCEPTION_EVENT_TYPE = "action_exception_event";
	private Class actionClass;
	private Method actionMethod;

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
			Boolean nullAllowed) {
		try {
			logger.info("run method invocation");

			Object[] actualParameters = extractMethodData(executionContext, executionRuntimeServices, className, methodName);

			nullAllowed = nullAllowed == null ? true : nullAllowed;
			verifyActionInputs(actualParameters, nullAllowed);

			Map<String, String> results = invokeMethod(executionRuntimeServices, className, methodName, actualParameters);

			mergeBackResults(executionContext, executionRuntimeServices, methodName, results);
		} catch (Exception ex) {
			executionRuntimeServices.addEvent(ACTION_EXCEPTION_EVENT_TYPE, ex);
		}
	}

	private void verifyActionInputs(Object[] actualParameters, boolean nullAllowed) {
		Class<?>[] parameterTypes =  actionMethod.getParameterTypes();
		boolean validParameters = InputBindingUtility.validateParameterArray(parameterTypes, actualParameters, nullAllowed);
		if (!validParameters) {
			List<BindingConflict> conflicts = InputBindingUtility.getBindingConflicts(parameterTypes, actualParameters, nullAllowed);
			throw new InputBindingUtility.InputBindingException(conflicts.toString());
		}
	}

	private void mergeBackResults(Map<String, Serializable> executionContext, ExecutionRuntimeServices executionRuntimeServices, String methodName, Map<String, String> results) {
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
			invokeMessage += " with parameters: ";
			int limit = actualParameters.length - 1;
			for (int i = 0; i < limit; i++) {
				String parameter = actualParameters[i] == null ? "null" : actualParameters[i].toString();
				invokeMessage += parameter + ",";
			}
			invokeMessage += actualParameters[actualParameters.length - 1];
		}

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
		String[] parameterNames = parameterNameDiscoverer.getParameterNames(actionMethod);

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
