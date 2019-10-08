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
import io.cloudslang.engine.versioning.services.VersionService;
import io.cloudslang.orchestrator.services.CancelExecutionService;
import io.cloudslang.score.facade.execution.ExecutionActionResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;

public final class LargeMessagesMonitorServiceImpl implements LargeMessagesMonitorService {

    private static Logger logger = Logger.getLogger(LargeMessagesMonitorServiceImpl.class);

    @Autowired
    private CancelExecutionService cancelExecutionService;

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Autowired
    private VersionService versionService;

    @Override
    @Transactional
    public void monitor() {

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
                firstMsg.incMsgSeqId();

                toRetry.add(firstMsg);
            }
        }

        // retry
        if (toRetry.size() > 0) {
            logger.warn("Retrying " + toRetry.size() + " entries " + toRetry);

            long msgVersion = versionService.getCurrentVersion(VersionService.MSG_RECOVERY_VERSION_COUNTER_NAME);
            executionQueueRepository.insertExecutionQueue(toRetry, msgVersion);
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

        int retries = 0;
        ExecStatus status = ExecStatus.PENDING;

        for (int size = msgs.size(), i = 1; i < size; i++) {
            ExecutionMessage crt = msgs.get(i);
            ExecutionMessage prev = msgs.get(i - 1);
            if (crt.getMsgSeqId() == prev.getMsgSeqId() - 1 && crt.getStatus() == status) {
                if (status == ExecStatus.ASSIGNED) {
                    retries++;
                }

                status = status == ExecStatus.ASSIGNED ? ExecStatus.PENDING : ExecStatus.ASSIGNED;

            } else {
                break;
            }
        }

        return retries;
    }
}
