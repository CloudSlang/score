/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.orchestrator.services;

import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.ExecutionStep;
import io.cloudslang.score.api.ScoreDeprecated;
import io.cloudslang.score.api.TriggeringProperties;
import io.cloudslang.engine.data.IdentityGenerator;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.entities.RunningExecutionPlan;
import io.cloudslang.score.facade.services.RunningExecutionPlanService;
import io.cloudslang.score.lang.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;

/**
 * Created by peerme on 23/07/2014
 */
public class ScoreDeprecatedImpl implements ScoreDeprecated {

    @Autowired
    private ScoreTriggering scoreTriggering;

    @Autowired
    private IdentityGenerator idGenerator;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Autowired
    private ExecutionStateService executionStateService;

    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;

    @Override
    public Long generateExecutionId() {
        return idGenerator.next();
    }

    @Override
    public Long trigger(Long executionId, TriggeringProperties triggeringProperties) {
         return scoreTriggering.trigger(executionId, triggeringProperties);
    }

    @Override
    public Long reTrigger(SystemContext newSystemContext, byte[] executionObj) {
        Execution execution = executionMessageConverter.extractExecution(new Payload(executionObj));
        //We must refresh the system context with the new one in order to re-trigger
        execution.getSystemContext().clear();
        execution.getSystemContext().putAll(newSystemContext);

        //generate new execution id
        Long newExecutionId = idGenerator.next();
        execution.getSystemContext().setExecutionId(newExecutionId);
        execution.setExecutionId(newExecutionId);

        RunningExecutionPlan runningExecutionPlan = runningExecutionPlanService.readExecutionPlanById(execution.getRunningExecutionPlanId());
        ExecutionPlan executionPlan = runningExecutionPlan.getExecutionPlan();
        ExecutionPlan newExecutionPlan = cloneExecutionPlanWithoutSteps(executionPlan);

        List<ExecutionStep> executionSteps = new ArrayList<>(executionPlan.getSteps().values());
        executionSteps.get(1).setNavigationData(getNavigationWithNewNextStep(executionSteps.get(1), execution.getPosition()));
        newExecutionPlan.addSteps(executionSteps);

        Long newRunningExecPlanId = runningExecutionPlanService.createRunningExecutionPlan(newExecutionPlan, newExecutionId.toString());
        execution.setRunningExecutionPlanId(newRunningExecPlanId);
        execution.setPosition(1L);//set the position to the precondition step

        // create execution record in ExecutionSummary table
        executionStateService.createParentExecution(execution.getExecutionId());

        // create execution message
        ExecutionMessage message = createExecutionMessage(execution);
        queueDispatcher.dispatch(Collections.singletonList(message));

        return newExecutionId;
    }

    private ExecutionPlan cloneExecutionPlanWithoutSteps(ExecutionPlan executionPlan) {
        ExecutionPlan newExecutionPlan = new ExecutionPlan();
        newExecutionPlan.setBeginStep(executionPlan.getBeginStep());
        newExecutionPlan.setFlowUuid(executionPlan.getFlowUuid());
        newExecutionPlan.setLanguage(executionPlan.getLanguage());
        newExecutionPlan.setName(executionPlan.getName());
        newExecutionPlan.setSubflowsUUIDs(executionPlan.getSubflowsUUIDs());
        newExecutionPlan.setSysAccPaths(executionPlan.getSysAccPaths());
        newExecutionPlan.setWorkerGroup(executionPlan.getWorkerGroup());
        return newExecutionPlan;
    }

    private Map<String, Object> getNavigationWithNewNextStep(ExecutionStep executionStep, Long nextStep){
        Iterator i = executionStep.getNavigationData().entrySet().iterator();
        Map<String, Object> newPreconditionNavigationData = new HashMap<>();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            if (entry.getKey().equals("next")) {
                newPreconditionNavigationData.put("next", nextStep);
            } else {
                newPreconditionNavigationData.put((String) entry.getKey(), entry.getValue());
            }
        }
        return newPreconditionNavigationData;
    }

    @Override
    public SystemContext extractSystemContext(byte[] executionObjectSerialized) {
        Execution execution = executionMessageConverter.extractExecution(new Payload(executionObjectSerialized));
        return execution.getSystemContext();
    }

    private ExecutionMessage createExecutionMessage(Execution execution) {
        Payload payload = executionMessageConverter.createPayload(execution);

        return new ExecutionMessage(ExecutionMessage.EMPTY_EXEC_STATE_ID,
                ExecutionMessage.EMPTY_WORKER,
                WorkerNode.DEFAULT_WORKER_GROUPS[0],
                String.valueOf(execution.getExecutionId()),
                ExecStatus.PENDING, //start new run also in PENDING
                payload,
                0);
    }
}
