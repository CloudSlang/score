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

import io.cloudslang.engine.queue.entities.LargeExecutionMessage;
import io.cloudslang.engine.queue.repositories.LargeExecutionMessagesRepository;
import io.cloudslang.orchestrator.services.CancelExecutionService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

public final class LargeMessagesMonitorServiceImpl implements LargeMessagesMonitorService {

    private Logger logger = Logger.getLogger(getClass());

    private static Integer noRetries = Integer.getInteger("queue.message.reassign.number", 5);
    private static Integer messageMaxLifetime = Integer.getInteger("queue.message.lifetime", 30);   // min
    private static Long reassignMinTime = Long.getLong("queue.message.reassign.min.time", 100);    // millis

    @Autowired
    private CancelExecutionService cancelExecutionService;

    @Autowired
    private LargeExecutionMessagesRepository largeExecutionMessagesRepository;

    @Override
    public void monitor() {
        List<LargeExecutionMessage> all = largeExecutionMessagesRepository.findAll();

        if (logger.isDebugEnabled()) {
            logger.debug("Found " + all.size() + " entries");
        }

        for (LargeExecutionMessage lem: all) {
            if (messageShouldBeCanceled(lem)) {
                cancelMessage(lem);
            } else if (messageShouldBeReassigned(lem)) {
                clearAssignedWorker(lem);
            }
        }
    }

    private void clearAssignedWorker(LargeExecutionMessage lem) {
        if (logger.isDebugEnabled()) {
            logger.debug("Clearing assigned worker for id: " + lem.getId());
        }

        largeExecutionMessagesRepository.clearAssignedWorker(lem.getId());
    }

    private void cancelMessage(LargeExecutionMessage lem) {
        long executionId = largeExecutionMessagesRepository.getMessageRunningExecutionId(lem.getId());

        if (logger.isDebugEnabled()) {
            logger.debug("Canceling " + lem.getId() + " executionId: " + executionId);
        }

        if (executionId != -1) {
            cancelExecutionService.requestCancelExecution(executionId);
        }

        largeExecutionMessagesRepository.delete(lem.getId());
    }

    private boolean messageShouldBeCanceled(LargeExecutionMessage lem) {
        return lem.getRetriesCount() >= noRetries || messageExceededLifetime(lem);
    }

    private boolean messageExceededLifetime(LargeExecutionMessage lem) {
        return getMessageLifetime(lem) / 60 > messageMaxLifetime;
    }

    private long getMessageLifetime(LargeExecutionMessage lem) {
        return Instant.now().getEpochSecond() - lem.getCreateTime() / 1000;
    }

    private boolean messageShouldBeReassigned(LargeExecutionMessage lem) {
        return System.currentTimeMillis() - lem.getUpdateTime() > reassignMinTime;
    }
}
