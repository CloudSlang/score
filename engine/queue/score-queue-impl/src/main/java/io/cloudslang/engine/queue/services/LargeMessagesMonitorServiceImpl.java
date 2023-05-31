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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;

public final class LargeMessagesMonitorServiceImpl implements LargeMessagesMonitorService {

    private static Logger logger = LogManager.getLogger(LargeMessagesMonitorServiceImpl.class);

    @Autowired
    private CancelExecutionService cancelExecutionService;

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Autowired
    private ExecutionQueueService execQueue;

    @Override
    @Transactional
    public void monitor() {
        if (!parseBoolean(getProperty("score.poll.use.large.message.query", "true"))) {
            return;
        }

        int noRetries = getNoRetries();

        long time = System.currentTimeMillis() - getMessageExpirationTime() * 1000;

        List<ExecutionMessage> messages = executionQueueRepository.findOldMessages(time);

        Map<Long, List<ExecutionMessage>> execStateMap = messages.stream().
                collect(groupingBy(ExecutionMessage::getExecStateId));

        Set<Long> toCancel = new HashSet<>();
        List<ExecutionMessage> toRetry = new ArrayList<>();

        for (Map.Entry<Long, List<ExecutionMessage>> entry : execStateMap.entrySet()) {

            long execStateId = entry.getKey();
            List<ExecutionMessage> msgs = entry.getValue();

            Collections.sort(msgs, comparingInt(ExecutionMessage::getMsgSeqId).reversed());

            if (countRetries(msgs) >= noRetries) {
                toCancel.add(execStateId);
            } else {
                ExecutionMessage firstMsg = msgs.get(0);
                firstMsg.setWorkerId(ExecutionMessage.EMPTY_WORKER);
                firstMsg.setStatus(ExecStatus.PENDING);
                toRetry.add(firstMsg);
            }
        }

        // retry
        if (toRetry.size() > 0) {
            logger.warn("Retrying " + toRetry.size() + " entries " + toRetry);
            execQueue.enqueue(toRetry);
        }

        // cancel
        if (toCancel.size() > 0) {
            Set<Long> execIds = executionQueueRepository.getExecutionIdsForExecutionStateIds(toCancel);

            logger.warn("Canceling execution with ids " + execIds);

            for (Long executionId : execIds) {
                ExecutionActionResult result = cancelExecutionService.requestCancelExecution(executionId);
                logger.warn("Requested cancel of execution id: " + executionId + ", result: " + result);
            }
        }
    }

    private int countRetries(List<ExecutionMessage> msgs) {

        if (msgs.size() == 0) {
            return 0;
        }

        int retries = 0;
        if (msgs.get(0).getStatus() == ExecStatus.ASSIGNED) {
            retries = 1;
        }

        int size = msgs.size();
        int i = 1;
        while (i < size) {
            ExecutionMessage crt = msgs.get(i);
            ExecutionMessage prev = msgs.get(i - 1);
            if (crt.getStatus() == ExecStatus.ASSIGNED && crt.getMsgSeqId() == prev.getMsgSeqId() - 1) {
                retries++;
            } else {
                break;
            }

            i++;
        }

        return retries;
    }
}
