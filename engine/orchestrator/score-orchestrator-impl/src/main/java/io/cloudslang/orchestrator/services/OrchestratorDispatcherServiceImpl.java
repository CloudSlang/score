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

import io.cloudslang.engine.node.services.WorkerLockService;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.entities.ExecutionMessage;
import io.cloudslang.engine.queue.services.QueueDispatcherService;
import io.cloudslang.orchestrator.entities.SplitMessage;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Date: 12/1/13
 */
public final class OrchestratorDispatcherServiceImpl implements OrchestratorDispatcherService {

    private final Logger logger = LogManager.getLogger(getClass());

    @Autowired
    @Qualifier("queueDispatcherService")
    private QueueDispatcherService queueDispatcher;

    @Autowired
    @Qualifier("splitJoinService")
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

        String currentBulkNumber = workerNodeService.readByUUID(workerUuid).getBulkNumber();
        //can not be null at this point
        String currentWRV = workerNodeService.readByUUID(workerUuid).getWorkerRecoveryVersion();

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
        Validate.notNull(messages, "Messages list is null");

        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching " + messages.size() + " messages");
        }

        long t = System.currentTimeMillis();
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

        int dispatched = toDispatchMessages.size() + toSplitMessages.size();
        int count = messages.size() - dispatched;
        t = System.currentTimeMillis() - t;

        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching " + dispatched + " messages is done in " + t + " ms");
        }
        if (count > 0) {
            logger.warn(count + " messages were not being dispatched, since unknown type");
        }
    }

}
