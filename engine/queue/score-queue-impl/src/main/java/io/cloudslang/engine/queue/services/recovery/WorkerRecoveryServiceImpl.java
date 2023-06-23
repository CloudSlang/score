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
package io.cloudslang.engine.queue.services.recovery;

import io.cloudslang.score.api.nodes.WorkerStatus;
import io.cloudslang.engine.node.entities.WorkerNode;
import io.cloudslang.engine.node.services.LoginListener;
import io.cloudslang.engine.node.services.WorkerLockService;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.engine.queue.services.ExecutionQueueService;
import io.cloudslang.engine.versioning.services.VersionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 8/6/14
 * Time: 9:17 AM
 */
public class WorkerRecoveryServiceImpl implements WorkerRecoveryService, LoginListener {

    private final Logger logger = LogManager.getLogger(getClass());

    static final int DEFAULT_POLL_SIZE = 1000;

    static final private long maxAllowedGap = Long.getLong("max.allowed.version.gap.msg.recovery", 10); //This is the max allowed gap
    // of versions for msg acknowledge, please note that this param with the rate of the version job, determines the time gap for msg recovery!

    @Autowired
    private WorkerNodeService workerNodeService;

    @Autowired
    private ExecutionQueueService executionQueueService;

    @Autowired
    private MessageRecoveryService messageRecoveryService;

    @Autowired
    private WorkerLockService workerLockService;

    @Autowired
    private VersionService versionService;

    @Override
    @Transactional
    public void doWorkerAndMessageRecovery(final String workerUuid) {

        //lock this worker to synchronize with drain action
        workerLockService.lock(workerUuid);

        List<String> workerUuids = workerNodeService.readNonRespondingWorkers();
        int messagesCount = getMessagesWithoutAck(DEFAULT_POLL_SIZE, workerUuid);
        WorkerNode worker = workerNodeService.findByUuid(workerUuid);
        if (worker.getStatus().equals(WorkerStatus.IN_RECOVERY) || workerUuids.contains(workerUuid) || messagesCount > 0) {
            if(workerUuids.contains(workerUuid)){
                logger.warn("Worker : " + workerUuid + " is non responsive! Worker recovery is started.");
            }
            if(messagesCount > 0){
                logger.warn("Worker : " + workerUuid + " has " + messagesCount + " not acknowledged messages. Worker recovery is started.");
            }
            if (worker.getStatus().equals(WorkerStatus.IN_RECOVERY)){
                logger.warn("Worker : " + workerUuid + " is IN_RECOVERY status. Worker recovery is started");
            }
            doWorkerRecovery(workerUuid);
        }
        else {
            logger.debug("Worker : " + workerUuid + " is NOT for recovery");
        }
    }

    @Override
    @Transactional
    public void doWorkerRecovery(String workerUuid) {

        //lock this worker to synchronize with drain action
        workerLockService.lock(workerUuid);

        logger.warn("Worker [" + workerUuid + "] is going to be recovered");
        long time = System.currentTimeMillis();
        // change status to in_recovery in separate transaction in order to make it as quickly as possible
        // so keep-alive wont be stuck and assigning won't take this worker as candidate
        workerNodeService.updateStatusInSeparateTransaction(workerUuid, WorkerStatus.IN_RECOVERY);

        final AtomicBoolean shouldContinue = new AtomicBoolean(true);

        while (shouldContinue.get()) {
            shouldContinue.set(messageRecoveryService.recoverMessagesBulk(workerUuid, DEFAULT_POLL_SIZE));
        }

        String newWRV = UUID.randomUUID().toString();
        workerNodeService.updateWRV(workerUuid, newWRV);
        workerNodeService.updateStatus(workerUuid, WorkerStatus.RECOVERED);

        logger.warn("Worker [" + workerUuid + "] recovery is done in " + (System.currentTimeMillis() - time) + " ms");
    }

    private int getMessagesWithoutAck(int maxSize, String workerUuid) {
        if (logger.isDebugEnabled()) logger.debug("Getting messages count without ack for worker: " + workerUuid);

        long systemVersion = versionService.getCurrentVersion(VersionService.MSG_RECOVERY_VERSION_COUNTER_NAME);
        long minVersionAllowed = Math.max( systemVersion - maxAllowedGap , 0);
        int result = executionQueueService.countMessagesWithoutAckForWorker(maxSize, minVersionAllowed, workerUuid);

        if (logger.isDebugEnabled()) logger.debug("Messages without ack found: " + result + " for worker: " + workerUuid);

        return result;
    }

    @Override
    @Transactional
    public void preLogin(String uuid) {
        logger.warn("Worker [" + uuid + "] is up and will be recovered");
        doWorkerRecovery(uuid);
    }

    @Override
    @Transactional
    public void postLogin(String uuid) {
        // Noting to-do
    }
}
