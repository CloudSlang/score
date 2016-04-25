/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.orchestrator.services;

import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.entities.ExecutionMessageConverter;
import io.cloudslang.engine.queue.entities.Payload;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.score.api.ScoreDeprecated;
import io.cloudslang.score.api.TriggeringProperties;
import io.cloudslang.engine.data.IdentityGenerator;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.lang.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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

        // create execution record in ExecutionSummary table
        executionStateService.createParentExecution(execution.getExecutionId());

        // create execution message
        ExecutionMessage message = createExecutionMessage(execution);
        queueDispatcher.dispatch(Collections.singletonList(message));

        return newExecutionId;
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
