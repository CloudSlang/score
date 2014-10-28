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
package com.hp.score.lang.tests.runtime.builders;

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionPlan;
import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.runtime.ActionType;
import com.hp.score.lang.runtime.Navigations;
import com.hp.score.lang.runtime.POCControlActions;
import com.hp.score.lang.tests.runtime.actions.LangActions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 06/10/2014
 * Time: 09:34
 */
public class POCExecutionPlanActionsBuilder {

    public static final String CONTROL_ACTION_CLASS_NAME = POCControlActions.class.getName();
    public static final String ACTION_CLASS_KEY = "className";
    public static final String ACTION_METHOD_KEY = "methodName";
    public static final String NEXT_STEP_ID_KEY = "nextStepId";
    public static final String NAVIGATION_ACTIONS_CLASS = Navigations.class.getName();
    public static final String SIMPLE_NAVIGATION_METHOD = "navigate";

    ExecutionPlan executionPlan;

    private Long index = 1L;

    public POCExecutionPlanActionsBuilder(){
        createExecutionPlan();
    }

    private void createExecutionPlan(){
        executionPlan = new ExecutionPlan();
        executionPlan.setFlowUuid("childFlow");
        executionPlan.setBeginStep(1L);
        executionPlan.addStep(createFlowPreActionStep());
        addFirstStep();
        addSecondStep();
        executionPlan.addStep(createFlowPostActionStep());
    }

    private void addFirstStep() {
        executionPlan.addStep(createPreOperationStep());
        executionPlan.addStep(createPreActionStep());
        executionPlan.addStep(createActionStep(LangActions.class.getName(), "parseUrl"));
        executionPlan.addStep(createPostActionStep());
        executionPlan.addStep(createPostOperationStep());
    }

    private void addSecondStep() {
        executionPlan.addStep(createPreOperationStep());
        executionPlan.addStep(createPreActionStep());
        executionPlan.addStep(createActionStep(LangActions.class.getName(), "parseUrl"));
        executionPlan.addStep(createPostActionStep());
        executionPlan.addStep(createPostOperationStep());
    }

    public ExecutionPlan getExecutionPlan(){
        return executionPlan;
    }

    private ExecutionStep createFlowPreActionStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> flowInputs = createFlowInputs();
        actionData.put("operationInputs", flowInputs);
        return createGeneralStep(index, CONTROL_ACTION_CLASS_NAME, "preAction", ++index, actionData);
    }

    private ExecutionStep createPreOperationStep(){
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> taskInputs = createPreOperationTaskInputs();
        actionData.put("taskInputs", taskInputs);
        return createGeneralStep(index, CONTROL_ACTION_CLASS_NAME, "preOperation", ++index, actionData);
    }

    private ExecutionStep createPreActionStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> operationInputs = createOperationInputs();
        actionData.put("operationInputs", operationInputs);
        return createGeneralStep(index, CONTROL_ACTION_CLASS_NAME, "preAction", ++index, actionData);
    }

    private ExecutionStep createActionStep(String actionClassName, String actionMethodName) {
        Map<String, Serializable> actionData = new HashMap<>();
        //put the actual action class name and method name
        actionData.put(ACTION_CLASS_KEY, actionClassName);
        actionData.put(ACTION_METHOD_KEY, actionMethodName);
        actionData.put("actionType", ActionType.JAVA);
        return createGeneralStep(index, CONTROL_ACTION_CLASS_NAME, "doAction", ++index, actionData);
    }

    private ExecutionStep createPostActionStep() {
        Map<String, Serializable> actionData = new HashMap<>();
        HashMap<String, Serializable> operationOutputs = createOperationOutputs();
        actionData.put("operationOutputs", operationOutputs);
        HashMap<String, Serializable> operationAnswers = createOperationAnswers();
        actionData.put("operationAnswers", operationAnswers);
        return createGeneralStep(index, CONTROL_ACTION_CLASS_NAME, "postAction", ++index, actionData);
    }

    private ExecutionStep createPostOperationStep(){
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

    private HashMap<String,Serializable> createPreOperationTaskInputs() {
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

    private HashMap<String, Serializable> createOperationInputs() {
        LinkedHashMap<String, Serializable> operationInputs = new LinkedHashMap<>();
        operationInputs.put("host", "$task_host");
        operationInputs.put("port", "7777");
        return operationInputs;
    }

    private HashMap<String, Serializable> createOperationOutputs(){
        LinkedHashMap<String, Serializable> operationOutputs = new LinkedHashMap<>();
        operationOutputs.put("host", null);
        operationOutputs.put("myUrl", "retVal[url]");
        return operationOutputs;
    }

    private HashMap<String,Serializable> createOperationAnswers() {
        LinkedHashMap<String, Serializable> operationAnswers = new LinkedHashMap<>();
        operationAnswers.put("SUCCESS", "retVal[isTrue]");
        operationAnswers.put("FAIL", "retVal[isFalse]");
        return operationAnswers;
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
        flowAnswers.put("SUCCESS", null);
        flowAnswers.put("FAIL", null);
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
