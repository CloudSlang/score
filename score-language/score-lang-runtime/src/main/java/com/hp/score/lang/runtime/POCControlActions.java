package com.hp.score.lang.runtime;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.oo.sdk.content.plugin.GlobalSessionObject;
import com.hp.oo.sdk.content.plugin.SerializableSessionObject;
import com.hp.score.api.execution.ExecutionParametersConsts;
import org.apache.commons.collections.MapUtils;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User: stoneo
 * Date: 22/10/2014
 * Time: 09:25
 */
public class POCControlActions {

    private final String OPERATION_ANSWER = "OPERATION_ANSWER";

    public void preOperation(@Param("taskInputs") LinkedHashMap<String, Serializable> taskInputs,
                             @Param("runEnv") RunEnvironment runEnv,
                             Map<String, Serializable> actionData) {

        Map<String, Serializable> operationContext = createBindInputsTempContext(runEnv, taskInputs);

        //todo: hook

        updateCurrentContextAndPushParentToStack(runEnv, operationContext);
    }

    public void preAction(@Param("operationInputs") LinkedHashMap<String, Serializable> operationInputs,
                          @Param("runEnv") RunEnvironment runEnv,
                          Map<String, Serializable> actionData) {

        resolveGroups();

        Map<String, Serializable> actionContext = createBindInputsTempContext(runEnv, operationInputs);
        //todo: clone action context before updating
        runEnv.getCurrentContext().putAll(actionContext);

        //todo: hook

        updateCurrentContextAndPushParentToStack(runEnv, actionContext);

    }

    public void doAction(@Param("runEnv") RunEnvironment runEnv,
                         @Param("nonSerializableExecutionData") Map<String, Object> nonSerializableExecutionData,
                         ActionType actionType,
                         @Param("className") String className,
                         @Param("methodName") String methodName,
                          Map<String, Serializable> actionData) {

        Map<String, String> returnValue = new HashMap<>();
        switch (actionType){
            case JAVA:
                returnValue = runAction(runEnv.getCurrentContext(), nonSerializableExecutionData, className, methodName);
                break;
            case PYTHON:
                returnValue = new HashMap<>();
                break;
            default:
                break;
        }
        //todo: hook

        ReturnContext ret= new ReturnContext(returnValue, null);

        updateCurrentContext(runEnv, returnValue);
    }

    public void postAction(@Param("runEnv") RunEnvironment runEnv,
                                @Param("operationOutputs") LinkedHashMap<String, Serializable> operationOutputs,
                                @Param("operationAnswers") LinkedHashMap<String, Serializable> operationAnswers,
                                Map<String, Serializable> actionData) {


        //todo: do we need to bind outputs with the operation context or action context?
        Map<String, Serializable> actionResultContext = runEnv.getCurrentContext();
        Map<String, Serializable> operationContext = runEnv.getStack().popContext();
        
        Map<String, Serializable> operationResultContext = createOperationBindOutputsContext(operationContext, actionResultContext, operationOutputs);

        String answer = resolveOperationAnswer(actionResultContext, operationAnswers, runEnv);
        runEnv.setAnswer(answer);
//        operationResultContext.put(OPERATION_ANSWER, answer);

        //todo: hook

        updateCurrentContext(runEnv, operationResultContext);
    }

    public void postOperation(@Param("runEnv") RunEnvironment runEnv,
                              @Param("taskPublishValues") LinkedHashMap<String, Serializable> taskPublishValues,
                              LinkedHashMap<String, Long> taskNavigationValues,
                              Map<String, Serializable> actionData) {

        Map<String, Serializable> publishValues = createBindOutputsContext(runEnv.getCurrentContext(), taskPublishValues);

        //todo: hook

        Long position = calculateNextPosition(runEnv.getAnswer(), taskNavigationValues);

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();
        flowContext.putAll(publishValues);
        updateCurrentContext(runEnv, flowContext);
    }

    private Long calculateNextPosition(String answer, LinkedHashMap<String, Long> taskNavigationValues) {
        //todo: implement
        return taskNavigationValues.get(answer);
    }

    private Map<String, Serializable> createBindOutputsContext(Map<String, Serializable> operationResultContext, LinkedHashMap<String, Serializable> taskOutputs) {
        Map<String, Serializable> tempContext = new LinkedHashMap<>();
        if (taskOutputs != null) {
            for (Map.Entry<String, Serializable> output : taskOutputs.entrySet()) {
                String outputKey = output.getKey();
                Serializable outputValue = output.getValue();
                if (outputValue != null) {
                    // TODO: missing - evaluate script
                    String outputRetValue = (String)operationResultContext.get(outputValue);
                    tempContext.put(outputKey, outputRetValue);
                }
            }
        }
        return tempContext;
    }

    private Map<String, Serializable> createBindInputsTempContext(RunEnvironment runEnv, Map<String, Serializable> inputs) {
        Map<String, Serializable> currentContext = runEnv.getCurrentContext();
        Map<String, Serializable> tempContext = new LinkedHashMap<>();
        if (inputs != null) {
            for (Map.Entry<String, Serializable> input : inputs.entrySet()) {
                String inputKey = input.getKey();
                Serializable value = input.getValue();
                if (value != null){
                    if(value instanceof String && ((String) value).indexOf("$") == 0){
                        // assigning from another param
                        String paramName = ((String) value).substring(1);
                        tempContext.put(inputKey, currentContext.get(paramName));
                    } else {
                        tempContext.put(inputKey, value);
                    }
                } else {
                    tempContext.put(inputKey, currentContext.get(inputKey));
                }
            }
        }
        return tempContext;
    }

    private void updateCurrentContextAndPushParentToStack(RunEnvironment runEnvironment, Map<String, ? extends Serializable> newContext) {
        ContextStack contextStack = runEnvironment.getStack();
        Map<String, Serializable> currentContext = runEnvironment.getCurrentContext();
        contextStack.pushContext(currentContext);
        updateCurrentContext(runEnvironment, newContext);

    }

    private void updateCurrentContext(RunEnvironment runEnvironment, Map<String, ? extends Serializable> newContext) {
        //TODO: put a deep clone of the new context
        runEnvironment.setCurrentContext(newContext);
    }

    private void resolveGroups() {
    }

    private String resolveOperationAnswer(Map<String, Serializable> retValue, LinkedHashMap<String, Serializable> possibleAnswers, RunEnvironment runEnv) {
        String answer = runEnv.getAnswer();
        if(answer != null){
            return answer;
        }
        if(MapUtils.isNotEmpty(possibleAnswers)){
            Iterator iter = possibleAnswers.entrySet().iterator();
            while (iter.hasNext()){
                Map.Entry answerEntry = (Map.Entry)iter.next();
                Object value = answerEntry.getValue();
                if(value == null){

                }
                else if(eval(value, retValue))
                    return answerEntry.getKey().toString();
            }
            throw new RuntimeException("No answer");
        }
        return "SUCCESS";
    }

    private boolean eval(Object expression, Map<String, Serializable> retValue) {
        //todo: resolve expression
        return true;
    }

    private Map<String, Serializable> createOperationBindOutputsContext(Map<String, Serializable> context, Map<String, Serializable> retValue, Map<String, Serializable> outputs) {
        Map<String, Serializable> tempContext = new LinkedHashMap<>();
        if (outputs != null) {
            for (Map.Entry<String, Serializable> output : outputs.entrySet()) {
                String outputKey = output.getKey();
                Serializable outputValue = output.getValue();
                if (outputValue != null) {
                    // TODO: missing - evaluate script
                    String outputRetValue = (String)retValue.get(getRetValueKey((String)outputValue));
                    tempContext.put(outputKey, outputRetValue);
                } else {
                    //from inputs
                    tempContext.put(outputKey, context.get(outputKey));
                }
            }
        }
        return tempContext;
    }

    private String getRetValueKey(String outputValue){
        //todo: temp solution. currently removing the prefix of retVal[ and suffix of ]
        return outputValue.substring(7, outputValue.length()-1);
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
        Map<String, String> returnMap = (Map<String, String>) returnObject;
        if (returnMap == null) {
            throw new Exception("Action method did not return Map<String,String>");
        } else {
            return returnMap;
        }
    }

    private Object[] extractMethodData(Map<String, Serializable> currentContext, Map<String, Object> nonSerializableExecutionData, String className, String methodName)  {

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

    /**
     * Extracts the actual method of the action's class
     *
     * @param className
     * @param methodName method name of the actual action
     * @return actual method represented by Method object
     * @throws ClassNotFoundException
     */
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
        if (serializableSessionMapValue == null){
            serializableSessionMapValue = new HashMap<String, Serializable>();
            context.put(ExecutionParametersConsts.SERIALIZABLE_SESSION_CONTEXT, serializableSessionMapValue);
        }
        Serializable serializableSessionContextObject = ((Map<String, Serializable>)serializableSessionMapValue).get(key);
        if (serializableSessionContextObject == null) {
            serializableSessionContextObject = new SerializableSessionObject();
            //noinspection unchecked
            ((Map<String, Serializable>)serializableSessionMapValue).put(key, serializableSessionContextObject);
        }
        args.add(serializableSessionContextObject);
    }
}
