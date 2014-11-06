package com.hp.score.lang.runtime.steps;

import com.hp.oo.sdk.content.annotations.Param;
import com.hp.score.lang.ExecutionRuntimeServices;
import com.hp.score.lang.runtime.env.ReturnValues;
import com.hp.score.lang.runtime.env.RunEnvironment;
import org.apache.commons.collections.MapUtils;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.hp.score.lang.entities.ScoreLangConstants.*;
import static com.hp.score.lang.runtime.events.LanguageEventData.*;
import static com.hp.score.api.execution.ExecutionParametersConsts.*;

/**
 * User: stoneo
 * Date: 02/11/2014
 * Time: 10:24
 */
//todo: decide on a name that is suitable for both flow & operation
@Component
public class OperationSteps extends AbstractSteps {

    public final String ANSWER_SUCCESS = "SUCCESS";

    public void start(@Param(OPERATION_INPUTS_KEY) LinkedHashMap<String, Serializable> operationInputs,
                      @Param(RUN_ENV) RunEnvironment runEnv,
                      @Param(USER_INPUTS_KEY) HashMap<String, Serializable> userInputs,
                      @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("=======");
        System.out.println(" start ");
        System.out.println("=======");
        Map<String, Serializable> operationContext = new HashMap<>();

        resolveGroups();

        Map<String, Serializable> callArguments = runEnv.removeCallArguments();
        callArguments.putAll(userInputs);

        Map<String, Serializable> actionArguments = createBindInputsMap(callArguments, operationInputs, executionRuntimeServices);

        //todo: clone action context before updating
        operationContext.putAll(actionArguments);

        //done with the user inputs, don't want it to be available in next start steps..
        userInputs.clear();

        //todo: hook

        updateCallArgumentsAndPushContextToStack(runEnv, operationContext, actionArguments);

    }

    public void end(@Param(RUN_ENV) RunEnvironment runEnv,
                    @Param(OPERATION_OUTPUTS_KEY) LinkedHashMap<String, Serializable> operationOutputs,
                    @Param(OPERATION_ANSWERS_KEY) LinkedHashMap<String, Serializable> operationAnswers,
                    @Param(EXECUTION_RUNTIME_SERVICES) ExecutionRuntimeServices executionRuntimeServices) {

        System.out.println("=====");
        System.out.println(" end ");
        System.out.println("=====");
        Map<String, Serializable> operationContext = runEnv.getStack().popContext();
        ReturnValues actionReturnValues = runEnv.removeReturnValues();
		List<String> eventFields = Arrays.asList("operationOutputs", "operationAnswers", "actionReturnValues");
		List<Serializable> eventValues = Arrays.asList(operationOutputs, operationAnswers, actionReturnValues);
		fireEvent(executionRuntimeServices, EVENT_OUTPUT_START, "Output binding started", "path", eventFields, eventValues);
        String answer = actionReturnValues.getAnswer() != null ? actionReturnValues.getAnswer() : resolveOperationAnswer(actionReturnValues.getOutputs(), operationAnswers);

        Map<String, String> operationReturnOutputs = createOperationBindOutputsContext(operationContext, actionReturnValues.getOutputs(), operationOutputs);

        //todo: hook

        ReturnValues returnValues = new ReturnValues(operationReturnOutputs, answer);
        runEnv.putReturnValues(returnValues);
        fireEvent(executionRuntimeServices, EVENT_OUTPUT_END, "Output binding finished", "path", Arrays.asList(RETURN_VALUES), Arrays.asList((Serializable)returnValues));
        printReturnValues(returnValues);
    }

    private void resolveGroups() {
    }

    private String resolveOperationAnswer(Map<String, String> retValue, LinkedHashMap<String, Serializable> possibleAnswers) {
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
        return ANSWER_SUCCESS;
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
                    outputRetValue = (String) context.get(outputKey);
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

}
