package com.hp.score.lang.compiler;
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

import com.hp.score.api.ControlActionMetadata;
import com.hp.score.api.ExecutionStep;
import com.hp.score.lang.entities.ActionType;
import com.hp.score.lang.entities.ScoreLangConstants;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by orius123 on 05/11/14.
 */
@Component
public class ExecutionStepFactory {

    private static final String STEPS_PACKAGE = "com.hp.score.lang.runtime.steps";
    private static final String OPERATION_STEPS_CLASS = STEPS_PACKAGE + ".OperationSteps";
    private static final String ACTION_STEPS_CLASS = STEPS_PACKAGE + ".ActionSteps";
    private static final String NAVIGATION_ACTIONS_CLASS = "com.hp.score.lang.runtime.navigations.Navigations";
    private static final String SIMPLE_NAVIGATION_METHOD = "navigate";



    public ExecutionStep createStartStep(Long index, Map<String, Serializable> preOpData) {
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.OPERATION_INPUTS_KEY, preOpData.get(SlangTextualKeys.INPUTS_KEY));
        actionData.put(ScoreLangConstants.HOOKS, "TBD"); //todo add implementation for user custom hooks
        return createGeneralStep(index, OPERATION_STEPS_CLASS, "start", ++index, actionData);
    }

    public ExecutionStep createActionStep(Long index, Map<String, Serializable> actionData) {
        ActionType actionType = actionData.get(ScoreLangConstants.ACTION_CLASS_KEY) != null ? ActionType.JAVA : ActionType.PYTHON;
        actionData.put("actionType", actionType);
        return createGeneralStep(index, ACTION_STEPS_CLASS, "doAction", ++index, actionData);
    }

    public ExecutionStep createEndStep(Long index, Map<String, Serializable> postOpData) {
        Map<String, Serializable> actionData = new HashMap<>();
        actionData.put(ScoreLangConstants.OPERATION_OUTPUTS_KEY, postOpData.get(SlangTextualKeys.OUTPUTS_KEY));
        actionData.put(ScoreLangConstants.OPERATION_ANSWERS_KEY, postOpData.get(SlangTextualKeys.ANSWERS_KEY));
        actionData.put(ScoreLangConstants.HOOKS, "TBD"); //todo add implementation for user custom hooks
        return createGeneralStep(index, OPERATION_STEPS_CLASS, "end", null, actionData);
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
        navigationData.put(ScoreLangConstants.NEXT_STEP_ID_KEY, nextStepId);

        step.setNavigationData(navigationData);

        return step;
    }

}
