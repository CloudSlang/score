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
package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import io.cloudslang.orchestrator.services.CancelExecutionService;
import io.cloudslang.score.facade.execution.ExecutionActionResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public final class LargeMessagesMonitorServiceImpl implements LargeMessagesMonitorService {

    private Logger logger = Logger.getLogger(getClass());


    @Autowired
    private CancelExecutionService cancelExecutionService;

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Override
    public void monitor() {

        long now = System.currentTimeMillis();

        List<ExecutionMessage> messages = executionQueueRepository.findMessages(now, ExecStatus.ASSIGNED);

        if (logger.isDebugEnabled()) {
            logger.debug("Found " + messages.size() + " entries");
        }

        for (ExecutionMessage msg : messages) {
            if (messageShouldBeCanceled(msg)) {
                cancelMessage(msg);
            } else if (messageShouldBeReassigned(msg)) {
                clearAssignedWorker(msg);
            }
        }
    }

    private void clearAssignedWorker(ExecutionMessage msg) {
        if (logger.isDebugEnabled()) {
            logger.debug("Clearing assigned worker for id: " + msg.getId());
        }

        executionQueueRepository.clearAssignedWorker(msg);
    }

    private void cancelMessage(ExecutionMessage msg) {
        long executionId = executionQueueRepository.getMessageRunningExecutionId(msg);

        logger.warn("Canceling execution with id: " + executionId + " for message: " + msg);

        if (executionId != -1) {
            ExecutionActionResult result = cancelExecutionService.requestCancelExecution(executionId);
            logger.warn("Requested cancel of execution id: " + executionId + ", result: " + result);
        } else {
            logger.warn("Execution not found for message!");
        }
    }

    private boolean messageShouldBeCanceled(ExecutionMessage msg) {
        long dtSeconds = (System.currentTimeMillis() - msg.getCreateDate()) / 1000;
        return dtSeconds > getMessageExpirationTime();
    }

    private boolean messageShouldBeReassigned(ExecutionMessage msg) {
        long dtSeconds = (System.currentTimeMillis() - msg.getCreateDate()) / 1000;
        return dtSeconds > getMessageTimeOnWorker();
    }
}
