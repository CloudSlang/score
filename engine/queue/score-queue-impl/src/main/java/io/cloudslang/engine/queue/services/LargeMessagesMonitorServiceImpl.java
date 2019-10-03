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

import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import io.cloudslang.orchestrator.services.CancelExecutionService;
import io.cloudslang.score.facade.execution.ExecutionActionResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.partition;

public final class LargeMessagesMonitorServiceImpl implements LargeMessagesMonitorService {

    private static Logger logger = Logger.getLogger(LargeMessagesMonitorServiceImpl.class);

    private static final int PARTITION_SIZE = 250;

    @Autowired
    private CancelExecutionService cancelExecutionService;

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Override
    @Transactional
    public void monitor() {

        int noRetries = getNoRetries();

        long time = System.currentTimeMillis() - getMessageExpirationTime() * 1000;

        List<ExecutionMessage> messages = executionQueueRepository.findOldMessages(time);

        if (logger.isDebugEnabled()) {
            logger.debug("Found " + messages.size() + " entries");
        }

        Map<Long, List<ExecutionMessage>> execStateMap = messages.stream().collect(Collectors.groupingBy(ExecutionMessage::getExecStateId));

        Set<Long> toCancel = new HashSet<>();
        Set<Long> toRetry = new HashSet<>();

        execStateMap.forEach((execStateId, msgs) -> {
            int count = 0;
            for (int i = 1; i < msgs.size(); i++) {
                if (msgs.get(i).getMsgSeqId() == msgs.get(i - 1).getMsgSeqId() - 1) {
                    count++;
                } else {
                    count = 0;
                }

                if (count >= noRetries) {
                    toCancel.add(execStateId);
                    break;
                }
            }

            if (count < noRetries) {
                toRetry.add(execStateId);
            }
        });

        if (logger.isDebugEnabled()) {
            logger.debug("Canceling " + toCancel.size() + " entries " + toCancel);
            logger.debug("Retrying " + toRetry.size() + " entries " + toRetry);
        }

        // retry
        if (toRetry.size() > 0) {
            for (List<Long> execStateIds : partition(toRetry, PARTITION_SIZE)) {
                executionQueueRepository.clearAssignedWorker(execStateIds);
            }
        }

        // cancel
        if (toCancel.size() > 0) {
            List<Long> execIds = executionQueueRepository.getExecutionIdsForExecutionStateIds(toCancel);

            if (logger.isDebugEnabled()) {
                logger.debug("Canceling execution with ids " + execIds);
            }

            for (Long executionId : execIds) {
                ExecutionActionResult result = cancelExecutionService.requestCancelExecution(executionId);
                logger.warn("Requested cancel of execution id: " + executionId + ", result: " + result);
            }
        }
    }
}
