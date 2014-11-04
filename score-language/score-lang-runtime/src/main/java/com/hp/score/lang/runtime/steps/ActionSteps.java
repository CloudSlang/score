package com.hp.score.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import com.hp.score.api.execution.ExecutionParametersConsts;
import com.hp.score.lang.entities.ActionType;
import com.hp.score.lang.entities.ScoreLangConstants;
import com.hp.score.lang.runtime.ReturnValues;
import com.hp.score.lang.runtime.RunEnvironment;
import org.apache.log4j.Logger;
import org.python.core.PyStringMap;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static com.hp.score.lang.entities.ScoreLangConstants.*;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:25
 */
@Component
public class ActionSteps extends AbstractSteps{

    private static final Logger logger = Logger.getLogger(ActionSteps.class);

    @Autowired
    private PythonInterpreter interpreter;

    public void doAction(@Param("runEnv") RunEnvironment runEnv,
                         @Param("nonSerializableExecutionData") Map<String, Object> nonSerializableExecutionData,
                         ActionType actionType,
                         @Param(ScoreLangConstants.ACTION_CLASS_KEY) String className,
                         @Param(ScoreLangConstants.ACTION_METHOD_KEY) String methodName,
                         Map<String, Serializable> actionData) {

        System.out.println("================================");
        System.out.println("doAction");
        System.out.println("================================");
        Map<String, String> returnValue = new HashMap<>();
        Map<String, Serializable> callArguments = runEnv.removeCallArguments();

        try {
            switch (actionType) {
                case JAVA:
                    returnValue = runJavaAction(callArguments, nonSerializableExecutionData, className, methodName);
                    break;
                case PYTHON:
                    returnValue = prepareAndRunPythonAction(callArguments, actionData);
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }

        //todo: hook

        ReturnValues returnValues = new ReturnValues(returnValue, null);
        runEnv.putReturnValues(returnValues);
        printReturnValues(returnValues);

    }

    private Map<String, String> runJavaAction(Map<String, Serializable> currentContext,
                                              Map<String, Object> nonSerializableExecutionData,
                                              String className,
                                              String methodName) {

        Object[] actualParameters = extractMethodData(currentContext, nonSerializableExecutionData, className, methodName);

        try {
            return invokeActionMethod(className, methodName, actualParameters);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Object[] extractMethodData(Map<String, Serializable> currentContext, Map<String, Object> nonSerializableExecutionData, String className, String methodName) {

        //get the Method object
        Method actionMethod;
        try {
            actionMethod = getMethodByName(className, methodName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        //extract the parameters from execution context
        return resolveActionArguments(actionMethod, currentContext, nonSerializableExecutionData);
    }


    private Map<String, String> invokeActionMethod(String className, String methodName, Object... parameters) throws Exception {
        Method actionMethod = getMethodByName(className, methodName);
        Class actionClass = Class.forName(className);
        Object returnObject = actionMethod.invoke(actionClass.newInstance(), parameters);
        @SuppressWarnings("unchecked") Map<String, String> returnMap = (Map<String, String>) returnObject;
        if (returnMap == null) {
            throw new Exception("Action method did not return Map<String,String>");
        } else {
            return returnMap;
        }
    }

    private Method getMethodByName(String className, String methodName) throws ClassNotFoundException {
        Class actionClass = Class.forName(className);
        Method[] methods = actionClass.getDeclaredMethods();
        Method actionMethod = null;
        for (Method m : methods) {
            if (m.getName().equals(methodName)) {
                actionMethod = m;
            }
        }
        return actionMethod;
    }

    protected Object[] resolveActionArguments(Method actionMethod, Map<String, Serializable> currentContext, Map<String, Object> nonSerializableExecutionData) {
        List<Object> args = new ArrayList<>();

        int index = 0;
        for (Annotation[] annotations : actionMethod.getParameterAnnotations()) {
            index++;
            for (Annotation annotation : annotations) {
                if (annotation instanceof Param) {
                    if (actionMethod.getParameterTypes()[index - 1].equals(GlobalSessionObject.class)) {
                        handleNonSerializableSessionContextArgument(nonSerializableExecutionData, args, (Param) annotation);
                    } else if (actionMethod.getParameterTypes()[index - 1].equals(SerializableSessionObject.class)) {
                        handleSerializableSessionContextArgument(currentContext, args, (Param) annotation);
                    } else {
                        args.add(currentContext.get(((Param) annotation).value()));
                    }
                }
            }
            if (args.size() != index) {
                throw new RuntimeException("All action arguments should be annotated with @Param");
            }
        }
        return args.toArray(new Object[args.size()]);
    }

    private void handleNonSerializableSessionContextArgument(Map<String, Object> nonSerializableExecutionData, List<Object> args, Param annotation) {
        String key = annotation.value();
        Object nonSerializableSessionContextObject = nonSerializableExecutionData.get(key);
        if (nonSerializableSessionContextObject == null) {
            nonSerializableSessionContextObject = new GlobalSessionObject<>();
            nonSerializableExecutionData.put(key, nonSerializableSessionContextObject);
        }
        args.add(nonSerializableSessionContextObject);
    }

    private void handleSerializableSessionContextArgument(Map<String, Serializable> context, List<Object> args, Param annotation) {
        String key = annotation.value();
        Serializable serializableSessionMapValue = context.get(ExecutionParametersConsts.SERIALIZABLE_SESSION_CONTEXT);
        if (serializableSessionMapValue == null) {
            serializableSessionMapValue = new HashMap<String, Serializable>();
            context.put(ExecutionParametersConsts.SERIALIZABLE_SESSION_CONTEXT, serializableSessionMapValue);
        }
        @SuppressWarnings("unchecked") Serializable serializableSessionContextObject = ((Map<String, Serializable>) serializableSessionMapValue).get(key);
        if (serializableSessionContextObject == null) {
            serializableSessionContextObject = new SerializableSessionObject();
            //noinspection unchecked
            ((Map<String, Serializable>) serializableSessionMapValue).put(key, serializableSessionContextObject);
        }
        args.add(serializableSessionContextObject);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> prepareAndRunPythonAction(
            Map<String, Serializable> currentContext,
            Map<String, Serializable> actionData) throws Exception {

        //actionData contains the info about inputs and outputs
        List<String> inputList;
        if (actionData.containsKey(INPUT_LIST_KEY)) {
            inputList = (List<String>) actionData.get(INPUT_LIST_KEY);
        } else {
            inputList = new ArrayList<>();
        }
        Map<String, String> userOutputs;
        if (actionData.containsKey(USER_OUTPUTS_KEY)) {
            userOutputs = (Map<String, String>) actionData.get(USER_OUTPUTS_KEY);
        } else {
            userOutputs = new LinkedHashMap<>();
        }

        if (actionData.containsKey(PYTHON_SCRIPT_KEY)) {
            return runPythonAction(currentContext, (String) actionData.get(PYTHON_SCRIPT_KEY), inputList, userOutputs);
        }

        throw new Exception("Python script not found in action data");
    }

    //we need this method to be synchronized so we will ot have multiple scripts run in parallel on the same context
    private synchronized Map<String, String> runPythonAction(Map<String, Serializable> currentContext,
                                                             String script,
                                                             List<String> inputList,
                                                             Map<String, String> userOutputs) {

        Map<String, Serializable> userVars = extractPythonMethodData(currentContext, inputList);
        executePythonScript(interpreter, script, userVars);
        ReturnValues returnValues = extractPythonOutputs(interpreter, userOutputs);
        cleanInterpreter(interpreter);
        return returnValues.getOutputs();
    }

    // TODO remove this comment
    // do we need to check the following cases too?
    //handleNonSerializableSessionContextArgument and handleSerializableSessionContextArgument
    private Map<String, Serializable> extractPythonMethodData(Map<String, Serializable> currentContext, List<String> inputList) {
        Map<String, Serializable> inputMap = new LinkedHashMap<>();
        for (String inputKey : inputList) {
            inputMap.put(inputKey, currentContext.get(inputKey));
        }
        return inputMap;
    }

    private void executePythonScript(PythonInterpreter interpreter, String script, Map<String, Serializable> userVars) {
        Iterator varsIterator = userVars.entrySet().iterator();
        while (varsIterator.hasNext()) {
            Map.Entry pairs = (Map.Entry) varsIterator.next();
            String key = (String) pairs.getKey();
            String value = (String) pairs.getValue();
            value = evaluateExpression(interpreter, value);
            interpreter.set(key, value);
            varsIterator.remove();
        }

        interpreter.exec(script);
    }

    private String evaluateExpression(PythonInterpreter interpreter, String value) {
        if (value.startsWith("-> ")) value = interpreter.eval(value.replace("-> ", "")).toString();
        return value;
    }

    private ReturnValues extractPythonOutputs(PythonInterpreter interpreter, Map<String, String> userOutputs) {
        Map<String, String> evaluatedOutputs = new LinkedHashMap<>();
        Iterator outputsIterator = userOutputs.entrySet().iterator();
        while (outputsIterator.hasNext()) {
            Map.Entry pairs = (Map.Entry) outputsIterator.next();
            String key = (String) pairs.getKey();
            String value = (String) pairs.getValue();
            value = evaluateExpression(interpreter, value);
            evaluatedOutputs.put(key, value);
            outputsIterator.remove();
        }
        return new ReturnValues(evaluatedOutputs, null);
    }

    private void cleanInterpreter(PythonInterpreter interpreter) {
        interpreter.setLocals(new PyStringMap());
    }
}
