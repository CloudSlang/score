/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.orchestrator.services;

import org.openscore.api.ExecutionPlan;
import org.openscore.api.TriggeringProperties;
import io.cloudslang.engine.data.IdentityGenerator;
import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import org.openscore.facade.entities.Execution;
import org.openscore.facade.services.RunningExecutionPlanService;
import org.openscore.lang.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;

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
        Long runningExecutionPlanId = saveRunningExecutionPlans(triggeringProperties.getExecutionPlan(), triggeringProperties.getDependencies(), scoreSystemContext);
        scoreSystemContext.setExecutionId(executionId);
        Execution execution = new Execution(executionId, runningExecutionPlanId, triggeringProperties.getStartStep(), triggeringProperties.getContext(), scoreSystemContext);

        // create execution record in ExecutionSummary table
        executionStateService.createParentExecution(execution.getExecutionId());

        // create execution message
        ExecutionMessage message = createExecutionMessage(execution);
        enqueue(message);
        return executionId;
    }

    private Long saveRunningExecutionPlans(ExecutionPlan executionPlan, Map<String, ExecutionPlan> dependencies, SystemContext systemContext) {
        Map<String, Long> runningPlansIds = new HashMap<>();
        Map<String, Long> beginStepsIds = new HashMap<>();

        if(dependencies != null) {
            for (ExecutionPlan dependencyExecutionPlan : dependencies.values()) {
                String subFlowUuid = dependencyExecutionPlan.getFlowUuid();
                Long subFlowRunningId = runningExecutionPlanService.getOrCreateRunningExecutionPlan(dependencyExecutionPlan);
                runningPlansIds.put(subFlowUuid, subFlowRunningId);
                beginStepsIds.put(subFlowUuid, dependencyExecutionPlan.getBeginStep());
            }
        }

        // Adding the ids of the running execution plan of the parent + its begin step
        // since this map should contain all the ids of the running plans
        Long runningPlanId =  runningExecutionPlanService.getOrCreateRunningExecutionPlan(executionPlan);
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
                WorkerNode.DEFAULT_WORKER_GROUPS[0],
                String.valueOf(execution.getExecutionId()),
                ExecStatus.PENDING, //start new flow also in PENDING
                payload,
                0);
    }
}
