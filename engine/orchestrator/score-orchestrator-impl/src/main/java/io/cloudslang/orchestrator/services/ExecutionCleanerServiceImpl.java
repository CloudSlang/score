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

import io.cloudslang.engine.queue.entities.ExecutionStatesData;
import io.cloudslang.engine.queue.services.cleaner.QueueCleanerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.cloudslang.score.facade.execution.ExecutionStatus.*;
import static java.util.Arrays.asList;
import static org.springframework.util.CollectionUtils.isEmpty;

public class ExecutionCleanerServiceImpl implements ExecutionCleanerService {

    private final int MAX_BULK_SIZE = Integer.getInteger("execution.state.clean.job.bulk.size", 200);
    final private int QUEUE_BULK_SIZE = 500;
    private final int SPLIT_SIZE = 200;
    private static final long EXECUTION_STATE_INACTIVE_TIME_FINISHED = 3 * 60 * 60 * 1000L;
    private static final long EXECUTION_STATE_INACTIVE_TIME_CANCELED = 24 * 60 * 60 * 1000L;
    private static final long ORPHAN_EXECUTION_QUEUES_INACTIVE_TIME = 72 * 60 * 60 * 1000L; // 72 hours

    private static final Logger logger = LogManager.getLogger(ExecutionCleanerServiceImpl.class);

    @Autowired
    private ExecutionStateService executionStateService;

    @Autowired
    private QueueCleanerService queueCleanerService;

    @Override
    // @Transactional
    public void cleanExecutions() {
        try {
            performExecutionCleanup(MAX_BULK_SIZE);
        } catch (Exception e) {
            logger.error("Execution cleanup job failed!", e);
        }
    }

    private void performExecutionCleanup(Integer bulkSize) {
        // safely delete execution queues and state mappings for non-latest or unused states
        // from oo_execution_states and oo_execution_queues
        deleteUnusedExecutionStatesAndQueues();

        // remove all finished executions and related data (CANCELED, COMPLETED, SYSTEM_FAILURE)
        // from oo_execution_queues, oo_execs_states_execs_mappings, oo_execution_states and oo_execution_state
        deleteFinalizedExecutionData(bulkSize);

        // Clean up orphaned queue entries left from parallel or multi-instance execution from oo_execution_queues
        deleteOrphanExecutionQueues();
    }

    private void deleteUnusedExecutionStatesAndQueues() {
        try {
            Set<Long> ids = queueCleanerService.getFinishedExecStateIds();
            if (logger.isDebugEnabled()) {
                logger.debug("Will clean from queue the next execution state ids amount: " + ids.size());
            }

            List<ExecutionStatesData> latestExecutionStateData = queueCleanerService.getLatestExecutionStates();
            if (logger.isDebugEnabled()) {
                String execStates = latestExecutionStateData.stream()
                        .map(ExecutionStatesData::toString)
                        .collect(Collectors.joining(", "));
                logger.debug("Latest execution states before cleaning job: " + execStates);
            }
            Set<Long> execIds = new HashSet<>();

            for (Long id : ids) {
                execIds.add(id);
                if (execIds.size() >= QUEUE_BULK_SIZE) {
                    queueCleanerService.cleanUnusedSteps(execIds);
                    execIds.clear();
                }
            }

            if (execIds.size() > 0) {
                queueCleanerService.cleanUnusedSteps(execIds);
            }
        } catch (Exception e) {
            logger.error("Can't run queue cleaner job.", e);
        }
    }

    private void deleteFinalizedExecutionData(Integer bulkSize) {
        long timeLimitMillisFinished = System.currentTimeMillis() - EXECUTION_STATE_INACTIVE_TIME_FINISHED;
        long timeLimitMillisCanceled = System.currentTimeMillis() - EXECUTION_STATE_INACTIVE_TIME_CANCELED;

        for (int i = 1; i <= bulkSize / SPLIT_SIZE; i++) {
            PageRequest pageRequest = PageRequest.of(0, SPLIT_SIZE);
            List<Long> finishedStateIds = executionStateService
                    .findExecutionStateByStatusInAndUpdateTimeLessThanEqual(asList(COMPLETED, SYSTEM_FAILURE),
                            timeLimitMillisFinished, pageRequest);
            List<Long> canceledStateIds = executionStateService
                    .findExecutionStateByStatusInAndUpdateTimeLessThanEqual(asList(CANCELED),
                            timeLimitMillisCanceled, pageRequest);

            if (logger.isDebugEnabled()) {
                int finishedAndCanceledSize = finishedStateIds.size() + canceledStateIds.size();
                logger.debug("Will clean from queue and states the next finished and canceled execution state ids amount: "
                        + finishedAndCanceledSize);
            }

            List<Long> allFinishedAndCanceledIds = new ArrayList<>(finishedStateIds);
            allFinishedAndCanceledIds.addAll(canceledStateIds);
            if (!isEmpty(allFinishedAndCanceledIds)) {
                Set<Long> finishedAndCanceledStatesQueuesIds = queueCleanerService
                        .getExecutionStatesByFinishedMessageId(new HashSet<>(allFinishedAndCanceledIds));

                queueCleanerService.cleanFinishedSteps(finishedAndCanceledStatesQueuesIds);
                executionStateService.deleteExecutionStateByIds(allFinishedAndCanceledIds);
            }
        }
    }

    private void deleteOrphanExecutionQueues() {
        long timeLimitMillisOrphanExecutionQueues = System.currentTimeMillis() - ORPHAN_EXECUTION_QUEUES_INACTIVE_TIME;
        Set<Long> orphanExecutionQueuesIds = queueCleanerService
                .getOrphanQueues(timeLimitMillisOrphanExecutionQueues);
        if (logger.isDebugEnabled()) {
            String orphanIds = orphanExecutionQueuesIds.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
            logger.debug("Will clean from queue the orphan amount: " + orphanExecutionQueuesIds.size());
            logger.debug("Will clean from queue the following orphans: " + orphanIds);
        }

        queueCleanerService.cleanOrphanQueues(orphanExecutionQueuesIds);
    }
}