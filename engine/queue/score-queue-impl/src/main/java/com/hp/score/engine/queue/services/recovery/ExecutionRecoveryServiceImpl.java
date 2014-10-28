/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package com.hp.score.engine.queue.services.recovery;

import com.hp.score.engine.node.services.WorkerNodeService;
import com.hp.score.engine.queue.entities.ExecStatus;
import com.hp.score.engine.queue.entities.ExecutionMessage;
import com.hp.score.engine.queue.services.ExecutionQueueService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * User: Amit Levin
 * Date: 20/11/12
 */
final public class ExecutionRecoveryServiceImpl implements ExecutionRecoveryService {

    private final Logger logger = Logger.getLogger(getClass());

    static final int DEFAULT_POLL_SIZE = 1000;

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private ExecutionQueueService executionQueueService;

    @Autowired
    private WorkerRecoveryService workerRecoveryService;

    @Autowired
    private MessageRecoveryService messageRecoveryService;

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public void doRecovery() {
        if (logger.isDebugEnabled()) {
            logger.debug("Begin recovery");
        }
        recoverWorkers();
        assignRecoveredMessages();

        if (logger.isDebugEnabled()) {
            logger.debug("End recovery");
        }
    }

    protected void recoverWorkers() {
        if (logger.isDebugEnabled()) {
            logger.debug("Workers recovery is being started");
        }
        long time = System.currentTimeMillis();
        // Recovery for all workers
        List<String> workersUuids = workerNodeService.readAllWorkersUuids();

        for (String workerUuid : workersUuids) {
            try {
                workerRecoveryService.doWorkerAndMessageRecovery(workerUuid);
            } catch (Exception ex) {
                logger.error("Failed to recover worker [" + workerUuid + "]", ex);
            }
        }
        if (logger.isDebugEnabled()) logger.debug("Workers recovery is done in " + (System.currentTimeMillis() - time) + " ms");
    }

    protected void assignRecoveredMessages() {
        if (logger.isDebugEnabled()) logger.debug("Reassigning recovered messages is being started");
        long time = System.currentTimeMillis();
        final AtomicBoolean shouldContinue = new AtomicBoolean(true);
        while (shouldContinue.get()) {
                    List<ExecutionMessage> messages = executionQueueService.readMessagesByStatus(DEFAULT_POLL_SIZE, ExecStatus.RECOVERED);
                    messageRecoveryService.enqueueMessages(messages, ExecStatus.PENDING);
                    shouldContinue.set(messages != null && messages.size() == DEFAULT_POLL_SIZE);
        }
        if (logger.isDebugEnabled()) logger.debug("Reassigning recovered messages is done in " + (System.currentTimeMillis() - time) + " ms");
    }
}
