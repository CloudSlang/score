package com.hp.score.lang.tests.runtime.builders;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.runtime.Navigations;
import com.hp.score.lang.runtime.POCControlActions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 06/10/2014
 * Time: 09:34
 */
public class POCParentExecutionPlanActionsBuilder {

    public static final String CONTROL_ACTION_CLASS_NAME = POCControlActions.class.getName();
    public static final String NEXT_STEP_ID_KEY = "nextStepId";
    public static final String NAVIGATION_ACTIONS_CLASS = Navigations.class.getName();
    public static final String SIMPLE_NAVIGATION_METHOD = "navigate";

    ExecutionPlan executionPlan;

    private Long index = 1L;

    public POCParentExecutionPlanActionsBuilder() {
        createExecutionPlan();
    }

    private void createExecutionPlan() {
        executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("parentFlow");
        executionPlan.setBeginStep(1L);
        executionPlan.addStep(createFlowPreActionStep());
        executionPlan.addStep(createPreOperationStep());
        executionPlan.addStep(createPostOperationStep());
        executionPlan.addStep(createFlowPostActionStep());
    }

    public ExecutionPlan getExecutionPlan() {
        return executionPlan;
    }

    private ExecutionStep createFlowPreActionStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> flowInputs = createFlowInputs();
        actionData.put("operationInputs", flowInputs);
        return createGeneralStep(index, CONTROL_ACTION_CLASS_NAME, "preAction", ++index, actionData);
    }

    private ExecutionStep createPreOperationStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> taskInputs = createPreOperationTaskInputs();
        actionData.put("taskInputs", taskInputs);

        ExecutionStep preOperationStep = createGeneralStep(index, CONTROL_ACTION_CLASS_NAME, "preOperation", ++index, actionData);
        HashMap<String, Object> navigationData = new HashMap<>(new HashMap<>(preOperationStep.getNavigationData()));
        navigationData.put("subFlowId", "childFlow");
        preOperationStep.setNavigationData(navigationData);

        return preOperationStep;
    }

    private ExecutionStep createPostOperationStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> taskPublishValues = createTaskPublishValues();
        actionData.put("taskPublishValues", taskPublishValues);
        HashMap<String, Long> taskNavigationValues = createTaskNavigationValues();
        actionData.put("taskNavigationValues", taskNavigationValues);
        return createGeneralStep(index, CONTROL_ACTION_CLASS_NAME, "postOperation", ++index, actionData);
    }

    private ExecutionStep createFlowPostActionStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> flowOutputs = createFlowOutputs();
        actionData.put("operationOutputs", flowOutputs);
        HashMap<String, Serializable> flowAnswers = createFlowAnswers();
        actionData.put("operationAnswers", flowAnswers);
        return createGeneralStep(index, CONTROL_ACTION_CLASS_NAME, "postAction", null, actionData);
    }

    private HashMap<String, Serializable> createPreOperationTaskInputs() {
        LinkedHashMap<String, Serializable> flowInputs = new LinkedHashMap<>();
        flowInputs.put("task_host", "hello");
        flowInputs.put("nova_port", null);
        return flowInputs;
    }

    private HashMap<String, Serializable> createFlowInputs() {
        LinkedHashMap<String, Serializable> flowInputs = new LinkedHashMap<>();
        flowInputs.put("nova_host", "host1");
        flowInputs.put("nova_port", "1234");
        return flowInputs;
    }

    private HashMap<String, Serializable> createTaskPublishValues() {
        LinkedHashMap<String, Serializable> taskPublishValues = new LinkedHashMap<>();
        taskPublishValues.put("host", null);
        taskPublishValues.put("task_url", "$url");
        return taskPublishValues;
    }

    private HashMap<String,Long> createTaskNavigationValues() {
        LinkedHashMap<String, Long> navigationValues = new LinkedHashMap<>();
        navigationValues.put("SUCCESS", null);
        navigationValues.put("FAIL", null);
        return navigationValues;
    }

    private HashMap<String, Serializable> createFlowOutputs() {
        LinkedHashMap<String, Serializable> flowOutputs = new LinkedHashMap<>();
        flowOutputs.put("flow_url", "$task_url");
        return flowOutputs;
    }

    private HashMap<String, Serializable> createFlowAnswers() {
        LinkedHashMap<String, Serializable> flowAnswers = new LinkedHashMap<>();
        //todo: how do I resolve the flow answer?
        flowAnswers.put("SUCCESS", "retVal[isTrue]");
        flowAnswers.put("FAIL", "retVal[isFalse]");
        return flowAnswers;
    }

    public ExecutionStep createGeneralStep(
            Long stepId,
            String actionClassName,
            String actionMethodName,
            Long nextStepId,
            Map<String, Serializable> actionData) {

        ExecutionStep step = new ExecutionStep(stepId);
        step.setAction(new ControlActionMetadata(actionClassName, actionMethodName));
        step.setActionData(actionData);

        step.setNavigation(new ControlActionMetadata(NAVIGATION_ACTIONS_CLASS, SIMPLE_NAVIGATION_METHOD));
        Map<String, Object> navigationData = new HashMap<>(2);
        navigationData.put(NEXT_STEP_ID_KEY, nextStepId);

        step.setNavigationData(navigationData);

//        step.setSplitStep(false);

        return step;
    }

}
