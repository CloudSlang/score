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
package com.hp.score.lang.runtime;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import com.hp.score.api.execution.ExecutionParametersConsts;
import org.apache.commons.collections.MapUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * User: stoneo
 * Date: 22/10/2014
 * Time: 09:25
 */
public class POCControlActions {

    private static int path = 0;
    private int PATH_SIZE = 100;

    public void beginTask(@Param("taskInputs") LinkedHashMap<String, Serializable> taskInputs,
                          @Param("runEnv") RunEnvironment runEnv,
                          Map<String, Serializable> actionData) {

        System.out.println("================================");
        System.out.println("path: " + ++path + " - beginTask");
        System.out.println("================================");

        runEnv.removeCallArguments();
        runEnv.removeReturnValues();

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        Map<String, Serializable> operationArguments = createBindInputsMap(flowContext, taskInputs);

        //todo: hook

        updateCallArgumentsAndPushContextToStack(runEnv, flowContext, operationArguments);
    }

    public void start(@Param("operationInputs") LinkedHashMap<String, Serializable> operationInputs,
                      @Param("runEnv") RunEnvironment runEnv,
                      HashMap<String, Serializable> userInputs,
                      Map<String, Serializable> actionData) {

        path *= PATH_SIZE;
        System.out.println("================================");
        System.out.println("path: " + path + " - start");
        System.out.println("================================");
        Map<String, Serializable> operationContext = new HashMap<>();

        resolveGroups();

        Map<String, Serializable> callArguments = runEnv.removeCallArguments();
        callArguments.putAll(userInputs);

        Map<String, Serializable> actionArguments = createBindInputsMap(callArguments, operationInputs);

        //todo: clone action context before updating
        operationContext.putAll(actionArguments);

        //done with the user inputs, don't want it to be available in next start steps..
        userInputs.clear();

        //todo: hook

        updateCallArgumentsAndPushContextToStack(runEnv, operationContext, actionArguments);

    }

    public void doAction(@Param("runEnv") RunEnvironment runEnv,
                         @Param("nonSerializableExecutionData") Map<String, Object> nonSerializableExecutionData,
                         ActionType actionType,
                         @Param("className") String className,
                         @Param("methodName") String methodName,
                         Map<String, Serializable> actionData) {

        System.out.println("================================");
        System.out.println("doAction");
        System.out.println("================================");
        Map<String, String> returnValue = new HashMap<>();
        Map<String, Serializable> callArguments = runEnv.removeCallArguments();
        switch (actionType) {
            case JAVA:
                returnValue = runAction(callArguments, nonSerializableExecutionData, className, methodName);
                break;
            case PYTHON:
                returnValue = new HashMap<>();
                break;
            default:
                break;
        }
        //todo: hook

        ReturnValues returnValues = new ReturnValues(returnValue, null);
        runEnv.putReturnValues(returnValues);
        printReturnValues(returnValues);

    }

    public void end(@Param("runEnv") RunEnvironment runEnv,
                    @Param("operationOutputs") LinkedHashMap<String, Serializable> operationOutputs,
                    @Param("operationAnswers") LinkedHashMap<String, Serializable> operationAnswers,
                    Map<String, Serializable> actionData) {


        System.out.println("================================");
        System.out.println("path: " + path + " - end");
        System.out.println("================================");
        path /= PATH_SIZE;
        Map<String, Serializable> operationContext = runEnv.getStack().popContext();
        ReturnValues actionReturnValues = runEnv.removeReturnValues();

        String answer = resolveOperationAnswer(actionReturnValues.getOutputs(), operationAnswers, actionReturnValues.getAnswer());

        Map<String, String> operationReturnOutputs = createOperationBindOutputsContext(operationContext, actionReturnValues.getOutputs(), operationOutputs);

        //todo: hook

        ReturnValues returnValues = new ReturnValues(operationReturnOutputs, answer);
        runEnv.putReturnValues(returnValues);
        printReturnValues(returnValues);
    }

    public void finishTask(@Param("runEnv") RunEnvironment runEnv,
                           @Param("taskPublishValues") LinkedHashMap<String, Serializable> taskPublishValues,
                           LinkedHashMap<String, Long> taskNavigationValues,
                           Map<String, Serializable> actionData) {

        System.out.println("================================");
        System.out.println("path: " + path + " - finishTask");
        System.out.println("================================");

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        ReturnValues operationReturnValues = runEnv.removeReturnValues();

        Map<String, Serializable> publishValues = createBindOutputsContext(operationReturnValues.getOutputs(), taskPublishValues);
        flowContext.putAll(publishValues);
        printMap(flowContext, "flowContext");

        //todo: hook

        Long position = calculateNextPosition(operationReturnValues.getAnswer(), taskNavigationValues);
        runEnv.putNextStepPosition(position);
        if (position == null) {
            ReturnValues returnValues = new ReturnValues(new HashMap<String, String>(), operationReturnValues.getAnswer());
            runEnv.putReturnValues(returnValues);
            printReturnValues(returnValues);
        }
        System.out.println("next position: " + position);

        runEnv.getStack().pushContext(flowContext);
    }

    private Long calculateNextPosition(String answer, LinkedHashMap<String, Long> taskNavigationValues) {
        //todo: implement
        return taskNavigationValues.get(answer);
    }

    private Map<String, Serializable> createBindOutputsContext(Map<String, String> operationResultContext, LinkedHashMap<String, Serializable> taskOutputs) {
        Map<String, Serializable> tempContext = new LinkedHashMap<>();
        if (taskOutputs != null) {
            for (Map.Entry<String, Serializable> output : taskOutputs.entrySet()) {
                String outputKey = output.getKey();
                Serializable outputValue = output.getValue();
                String outputRetValue = null;
                if (outputValue != null) {
                    if (outputValue instanceof String) {
                        // assigning from another param
                        String paramName = (String) outputValue;
                        // TODO: missing - evaluate script
                        outputRetValue = operationResultContext.get(paramName);
                        if (outputRetValue == null)
                            outputRetValue = paramName;
                    } else {
                        tempContext.put(outputKey, outputValue);
                    }
                } else {
                    outputRetValue = operationResultContext.get(outputKey);
                }
                tempContext.put(outputKey, outputRetValue);
            }
        }
        return tempContext;
    }

    private Map<String, Serializable> createBindInputsMap(Map<String, Serializable> callArguments, Map<String, Serializable> inputs) {
        Map<String, Serializable> tempContext = new LinkedHashMap<>();
        if (MapUtils.isEmpty(inputs)) return tempContext;
        for (Map.Entry<String, Serializable> input : inputs.entrySet()) {
            String inputKey = input.getKey();
            Serializable value = input.getValue();
            if (value != null) {
                if (value instanceof String) {
                    // assigning from another param
                    String paramName = (String) value;
                    Serializable callArgument = callArguments.get(paramName);
                    tempContext.put(inputKey, callArgument == null ? value : callArgument);
                } else {
                    tempContext.put(inputKey, value);
                }
            } else {
                tempContext.put(inputKey, callArguments.get(inputKey));
            }
        }
        return tempContext;
    }

    private void updateCallArgumentsAndPushContextToStack(RunEnvironment runEnvironment, Map<String, Serializable> currentContext, Map<String, Serializable> callArguments) {
        printMap(currentContext, "currentContext");
        printMap(callArguments, "callArguments");
        ContextStack contextStack = runEnvironment.getStack();
        contextStack.pushContext(currentContext);
        updateCallArguments(runEnvironment, callArguments);
    }

    private void printMap(Map<String, Serializable> map, String label) {
        if (MapUtils.isEmpty(map)) return;
        MapUtils.debugPrint(System.out, label, map);
        System.out.println("---------------------");
    }

    private void printReturnValues(ReturnValues returnValues){
        if(returnValues == null) return;
        MapUtils.debugPrint(System.out, "Return Values", returnValues.getOutputs());
        System.out.println("Answer: " + returnValues.getAnswer());
    }

    private void updateCallArguments(RunEnvironment runEnvironment, Map<String, Serializable> newContext) {
        //TODO: put a deep clone of the new context
        runEnvironment.putCallArguments(newContext);
    }

    private void resolveGroups() {
    }

    private String resolveOperationAnswer(Map<String, String> retValue, LinkedHashMap<String, Serializable> possibleAnswers, String answer) {
        if (answer != null) {
            return answer;
        }
        if (MapUtils.isNotEmpty(possibleAnswers)) {
            Iterator iter = possibleAnswers.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry answerEntry = (Map.Entry) iter.next();
                Object value = answerEntry.getValue();
                if (value == null) {

                } else if (eval(value, retValue))
                    return answerEntry.getKey().toString();
            }
            throw new RuntimeException("No answer");
        }
        return "SUCCESS";
    }

    private boolean eval(Object expression, Map<String, String> retValue) {
        //todo: resolve expression
        return true;
    }

    private Map<String, String> createOperationBindOutputsContext(Map<String, Serializable> context, Map<String, String> retValue, Map<String, Serializable> outputs) {
        Map<String, String> tempContext = new LinkedHashMap<>();
        if (outputs != null) {
            for (Map.Entry<String, Serializable> output : outputs.entrySet()) {
                String outputKey = output.getKey();
                Serializable outputValue = output.getValue();
                String outputRetValue = null;
                if (outputValue != null) {
                    if (outputValue instanceof String) {
                        // assigning from another param
                        String paramName = (String) outputValue;
                        // TODO: missing - evaluate script
                        outputRetValue = retValue.get(getRetValueKey(paramName));
                        if (outputRetValue == null)
                            outputRetValue = paramName;
                    }
                } else {
                    outputRetValue = (String)context.get(outputKey);
                }
                tempContext.put(outputKey, outputRetValue);
            }
        }
        return tempContext;
    }

    private String getRetValueKey(String outputValue) {
        //todo: temp solution. currently removing the prefix of retVal[ and suffix of ]
        return outputValue.substring(7, outputValue.length() - 1);
    }

    private Map<String, String> runAction(Map<String, Serializable> currentContext,
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

    private Object[] extractMethodData(Map<String, Serializable> currentContext, Map<String, Object> nonSerializableExecutionData, String className, String methodName) {

        //get the Method object
        Method actionMethod;
        try {
            actionMethod = getMethodByName(className, methodName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        //get the parameter names of the action method
//        ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
//        String[] parameterNames = parameterNameDiscoverer.getParameterNames(actionMethod);

        //extract the parameters from execution context
        return resolveActionArguments(actionMethod, currentContext, nonSerializableExecutionData);
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
}
