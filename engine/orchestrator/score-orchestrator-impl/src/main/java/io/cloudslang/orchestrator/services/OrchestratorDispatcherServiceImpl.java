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
import io.cloudslang.engine.node.services.WorkerLockService;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.orchestrator.entities.SplitMessage;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Date: 12/1/13
 */
public final class OrchestratorDispatcherServiceImpl implements OrchestratorDispatcherService {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private QueueDispatcherService queueDispatcher;

    @Autowired
    private SplitJoinService splitJoinService;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private WorkerLockService workerLockService;

    @Override
    @Transactional
    public void dispatch(List<? extends Serializable> messages, String bulkNumber, String wrv, String workerUuid) {
        //lock to synchronize with the recovery job
        workerLockService.lock(workerUuid);
        Validate.notNull(messages, "Messages list is null");

        WorkerNode workerNode = workerNodeService.readByUUID(workerUuid);
        String currentBulkNumber = workerNode.getBulkNumber();
        //can not be null at this point
        String currentWRV = workerNode.getWorkerRecoveryVersion();

        //This is done in order to make sure that if we do retries in worker we won't insert same bulk twice
        if (currentBulkNumber != null && currentBulkNumber.equals(bulkNumber)) {
            logger.warn("Orchestrator got messages bulk with same bulk number: " + bulkNumber
                    + " This bulk was inserted to DB before. Discarding...");
        }
        //This is done in order to make sure that we are not getting messages from worker that was already recovered and does not know about it yet
        else if (!currentWRV.equals(wrv)) {
            logger.warn("Orchestrator got messages from worker: " + workerUuid + " with wrong WRV:" + wrv
                    + " Current WRV is: " + currentWRV + ". Discarding...");
        } else {
            dispatch(messages);
            workerNodeService.updateBulkNumber(workerUuid, bulkNumber);
        }
    }

    private void dispatch(List<? extends Serializable> messages) {
        final List<ExecutionMessage> toDispatchMessages = messages.stream()
                .filter(ExecutionMessage.class::isInstance)
                .map(ExecutionMessage.class::cast)
                .collect(toList());
        queueDispatcher.dispatch(toDispatchMessages);

        final List<SplitMessage> toSplitMessages = messages.stream()
                .filter(SplitMessage.class::isInstance)
                .map(SplitMessage.class::cast)
                .collect(toList());
        splitJoinService.split(toSplitMessages);
    }

}
