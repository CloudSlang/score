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

import io.cloudslang.score.api.ExecutionPlan;
import io.cloudslang.score.api.TriggeringProperties;
import io.cloudslang.engine.data.IdentityGenerator;
import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.score.api.execution.ExecutionMetadataConsts;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.services.RunningExecutionPlanService;
import io.cloudslang.score.lang.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 30/01/14
 * Time: 14:19
 */
public class ScoreTriggeringImpl implements ScoreTriggering {

    @Autowired
    private RunningExecutionPlanService runningExecutionPlanService;

    @Autowired
    private IdentityGenerator idGenerator;

    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Autowired
    private ExecutionStateService executionStateService;

    @Override
    public Long trigger(TriggeringProperties triggeringProperties) {
        Long executionId = idGenerator.next();
        return trigger(executionId, triggeringProperties);
    }

    @Override
    public Long trigger(Long executionId, TriggeringProperties triggeringProperties) {
        SystemContext scoreSystemContext = new SystemContext(triggeringProperties.getRuntimeValues());
        Long runningExecutionPlanId = saveRunningExecutionPlans(triggeringProperties.getExecutionPlan(), triggeringProperties.getDependencies(), scoreSystemContext, String.valueOf(executionId));
        scoreSystemContext.setExecutionId(executionId);
        Map<String,Serializable> executionMetadata = createMetadata(triggeringProperties);
        scoreSystemContext.putMetaData(executionMetadata);
        Execution execution = new Execution(executionId, runningExecutionPlanId, triggeringProperties.getStartStep(), triggeringProperties.getContext(), scoreSystemContext);
        execution.setGroupName(triggeringProperties.getExecutionPlan().getWorkerGroup());

        // create execution record in ExecutionSummary table
        executionStateService.createParentExecution(execution.getExecutionId());

        // create execution message
        ExecutionMessage message = createExecutionMessage(execution);
        enqueue(message);
        return executionId;
    }

    private Map<String,Serializable> createMetadata(TriggeringProperties triggeringProperties){
        Map<String,Serializable> executionMetadata = new HashMap<>();
        ExecutionPlan executionPlan = triggeringProperties.getExecutionPlan();
        executionMetadata.put(ExecutionMetadataConsts.EXECUTION_PLAN_ID, executionPlan.getFlowUuid());
        executionMetadata.put(ExecutionMetadataConsts.EXECUTION_PLAN_NAME,executionPlan.getName());

        Map<String,Serializable> platformMetadata = (Map<String,Serializable>)triggeringProperties.getPlatformMetadata();
        if (platformMetadata != null){
            executionMetadata.putAll(platformMetadata);
        }

        return executionMetadata;
    }

    private Long saveRunningExecutionPlans(ExecutionPlan executionPlan, Map<String, ExecutionPlan> dependencies, SystemContext systemContext, String executionId) {
        Map<String, Long> runningPlansIds = new HashMap<>();
        Map<String, Long> beginStepsIds = new HashMap<>();

        if(dependencies != null) {
            for (ExecutionPlan dependencyExecutionPlan : dependencies.values()) {
                String subFlowUuid = dependencyExecutionPlan.getFlowUuid();
                Long subFlowRunningId = runningExecutionPlanService.createRunningExecutionPlan(dependencyExecutionPlan, executionId);
                runningPlansIds.put(subFlowUuid, subFlowRunningId);
                beginStepsIds.put(subFlowUuid, dependencyExecutionPlan.getBeginStep());
            }
        }

        // Adding the ids of the running execution plan of the parent + its begin step
        // since this map should contain all the ids of the running plans
        Long runningPlanId =  runningExecutionPlanService.createRunningExecutionPlan(executionPlan, executionId);
        runningPlansIds.put(executionPlan.getFlowUuid(), runningPlanId);
        beginStepsIds.put(executionPlan.getFlowUuid(), executionPlan.getBeginStep());

        systemContext.setSubFlowsData(runningPlansIds, beginStepsIds);

        return runningPlanId;
    }

    private void enqueue(ExecutionMessage... messages) {
        queueDispatcher.dispatch(Arrays.asList(messages));
    }

    private ExecutionMessage createExecutionMessage(Execution execution) {
        Payload payload = executionMessageConverter.createPayload(execution);

        return new ExecutionMessage(ExecutionMessage.EMPTY_EXEC_STATE_ID,
                ExecutionMessage.EMPTY_WORKER,
                execution.getGroupName() != null ? execution.getGroupName() : WorkerNode.DEFAULT_WORKER_GROUPS[0],
                String.valueOf(execution.getExecutionId()),
                ExecStatus.PENDING, //start new flow also in PENDING
                payload,
                0);
    }
}
