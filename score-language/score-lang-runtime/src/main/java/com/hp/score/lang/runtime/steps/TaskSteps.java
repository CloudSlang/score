package com.hp.score.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.runtime.ReturnValues;
import com.hp.score.lang.runtime.RunEnvironment;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:23
 */
@Component
public class TaskSteps extends AbstractSteps {

    public void beginTask(@Param("taskInputs") LinkedHashMap<String, Serializable> taskInputs,
                          @Param("runEnv") RunEnvironment runEnv,
                          Map<String, Serializable> actionData) {

        System.out.println("===========");
        System.out.println(" beginTask ");
        System.out.println("===========");

        runEnv.removeCallArguments();
        runEnv.removeReturnValues();

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        Map<String, Serializable> operationArguments = createBindInputsMap(flowContext, taskInputs);

        //todo: hook

        updateCallArgumentsAndPushContextToStack(runEnv, flowContext, operationArguments);
    }

    public void finishTask(@Param("runEnv") RunEnvironment runEnv,
                           @Param("taskPublishValues") LinkedHashMap<String, Serializable> taskPublishValues,
                           @Param("taskNavigationValues") LinkedHashMap<String, Long> taskNavigationValues,
                           Map<String, Serializable> actionData) {

        System.out.println("============");
        System.out.println(" finishTask ");
        System.out.println("============");

        Map<String, Serializable> flowContext = runEnv.getStack().popContext();

        ReturnValues operationReturnValues = runEnv.removeReturnValues();

        Map<String, Serializable> publishValues = createBindOutputsContext(operationReturnValues.getOutputs(), taskPublishValues);
        flowContext.putAll(publishValues);
        printMap(flowContext, "flowContext");

        //todo: hook

        Long nextPosition = calculateNextPosition(operationReturnValues.getAnswer(), taskNavigationValues);
        runEnv.putNextStepPosition(nextPosition);
        ReturnValues returnValues = new ReturnValues(new HashMap<String, String>(), operationReturnValues.getAnswer());
        runEnv.putReturnValues(returnValues);
        printReturnValues(returnValues);
        System.out.println("next position: " + nextPosition);

        runEnv.getStack().pushContext(flowContext);
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

    private Long calculateNextPosition(String answer, LinkedHashMap<String, Long> taskNavigationValues) {
        //todo: implement
        return taskNavigationValues.get(answer);
    }
}
