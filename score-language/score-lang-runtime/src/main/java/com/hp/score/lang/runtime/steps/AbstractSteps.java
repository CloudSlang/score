package com.hp.score.lang.runtime.steps;

import com.hp.score.lang.runtime.env.ContextStack;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;
import org.apache.commons.collections.MapUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractSteps {

    protected Map<String, Serializable> createBindInputsMap(Map<String, Serializable> callArguments, Map<String, Serializable> inputs) {
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

    protected void updateCallArgumentsAndPushContextToStack(RunEnvironment runEnvironment, Map<String, Serializable> currentContext, Map<String, Serializable> callArguments) {
        printMap(currentContext, "currentContext");
        printMap(callArguments, "callArguments");
        ContextStack contextStack = runEnvironment.getStack();
        contextStack.pushContext(currentContext);
        updateCallArguments(runEnvironment, callArguments);
    }

    protected void printMap(Map<String, Serializable> map, String label) {
//        if (MapUtils.isEmpty(map)) return;
//        MapUtils.debugPrint(System.out, label, map);
//        System.out.println("---------------------");
    }

    private void updateCallArguments(RunEnvironment runEnvironment, Map<String, Serializable> newContext) {
        //TODO: put a deep clone of the new context
        runEnvironment.putCallArguments(newContext);
    }

    protected void printReturnValues(ReturnValues returnValues) {
//        if (returnValues == null) return;
//        MapUtils.debugPrint(System.out, "Return Values", returnValues.getOutputs());
//        System.out.println("Answer: " + returnValues.getAnswer());
    }
}
