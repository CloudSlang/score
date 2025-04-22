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
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import io.cloudslang.engine.queue.services.cleaner.QueueCleanerService;
import io.cloudslang.orchestrator.repositories.ExecutionStateRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import static io.cloudslang.score.facade.execution.ExecutionStatus.*;
import static java.util.Arrays.asList;
import static org.springframework.util.CollectionUtils.isEmpty;

public class FinishedExecutionStateCleanerServiceImpl implements FinishedExecutionStateCleanerService {

    private final int MAX_BULK_SIZE = Integer.getInteger("execution.state.clean.job.bulk.size", 200);
    final private int QUEUE_BULK_SIZE = 500;
    private final int SPLIT_SIZE = 200;
    private static final long EXECUTION_STATE_INACTIVE_TIME = 5 * 60 * 1000L;
    private static final long ORPHAN_EXECUTION_QUEUES_INACTIVE_TIME = 10 * 60 * 1000L;

    private static final Logger logger = LogManager.getLogger(FinishedExecutionStateCleanerServiceImpl.class);

    @Autowired
    private ExecutionStateRepository executionStateRepository;

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Autowired
    private QueueCleanerService queueCleanerService;

    @Override
    @Transactional
    public void cleanFinishedExecutionState() {
        try {
            // cleanFinishedExecutionState(MAX_BULK_SIZE);
            cleanFinishedStateAndQueues(MAX_BULK_SIZE);
        } catch (Exception e) {
            logger.error("Finished execution state cleaner job failed!", e);
        }
    }

    private void cleanFinishedExecutionState(Integer bulkSize) {
        long timeLimitMillis = System.currentTimeMillis() - EXECUTION_STATE_INACTIVE_TIME;

        for (int i = 1; i <= bulkSize / SPLIT_SIZE; i++) {
            PageRequest pageRequest = PageRequest.of(0, SPLIT_SIZE);
            List<Long> toBeDeleted = executionStateRepository.findByStatusInAndUpdateTimeLessThanEqual(asList(CANCELED, COMPLETED, SYSTEM_FAILURE), timeLimitMillis, pageRequest);

            if (!isEmpty(toBeDeleted)) {
                executionStateRepository.deleteByIds(toBeDeleted);
            }

        }

    }

    private void cleanFinishedStateAndQueues(Integer bulkSize) {
        // delete incrementally from oo_execution_states, oo_execution_queues and oo_execs_states_execs_mappings
        deleteIncrementallyFromExecutionQueues();

        // delete from oo_execution_states, oo_execution_queues, oo_execs_states_execs_mappings, oo_execution_state
        // where oo_execution_state.status is finished (CANCELED, COMPLETED, SYSTEM_FAILURE)
        deleteFinishedExecutionsFromAllTables(bulkSize);

        // delete orphan oo_execution_queues which are left over from multi instance, blocking and parallel step execution
        deleteOrphanExecutionQueues();

        // delete from suspended_executions and finished_executions
    }

    private void deleteIncrementallyFromExecutionQueues() {
        try {
            List<ExecutionStatesData> latestExecutionStateData = executionQueueRepository.getLatestExecutionStates();
            if (logger.isDebugEnabled()) {
                String execStates = latestExecutionStateData.stream()
                        .map(ExecutionStatesData::toString)
                        .collect(Collectors.joining(", "));
                logger.debug("Latest execution states before cleaning job: " + execStates);
            }
            String execStates = latestExecutionStateData.stream()
                    .map(ExecutionStatesData::toString)
                    .collect(Collectors.joining(", "));
            logger.warn("Latest execution states before cleaning job: " + execStates);

            Set<Long> ids = queueCleanerService.getFinishedExecStateIds();
            if (logger.isDebugEnabled()) {
                logger.debug("Will clean from queue the next execution state ids amount: " + ids.size());
            }
            logger.warn("Will clean from queue the next execution state ids amount: " + ids.size());

            Set<Long> execIds = new HashSet<>();

            for (Long id : ids) {
                execIds.add(id);
                if (execIds.size() >= QUEUE_BULK_SIZE) {
                    queueCleanerService.cleanFinishedSteps(execIds);
                    execIds.clear();
                }
            }

            if (execIds.size() > 0) {
                queueCleanerService.cleanFinishedSteps(execIds);
            }
        } catch (Exception e) {
            logger.error("Can't run queue cleaner job.", e);
        }
    }

    private void deleteFinishedExecutionsFromAllTables(Integer bulkSize) {
        long timeLimitMillis = System.currentTimeMillis() - EXECUTION_STATE_INACTIVE_TIME;

        for (int i = 1; i <= bulkSize / SPLIT_SIZE; i++) {
            PageRequest pageRequest = PageRequest.of(0, SPLIT_SIZE);
            List<Long> finishedStateIds = executionStateRepository
                    .findByStatusInAndUpdateTimeLessThanEqual(asList(CANCELED, COMPLETED, SYSTEM_FAILURE),
                            timeLimitMillis, pageRequest);
            if (logger.isDebugEnabled()) {
                logger.debug("Will clean from queue and states the next finished execution state ids amount: "
                        + finishedStateIds.size());
            }
            logger.warn("Will clean from queue and states the next finished execution state ids amount: "
                    + finishedStateIds.size());

            if (!isEmpty(finishedStateIds)) {
                Set<Long> finishedStatesQueuesIds = executionQueueRepository
                        .getExecutionStatesByFinishedMessageId(new HashSet<>(finishedStateIds));

                executionQueueRepository.deleteFinishedSteps(finishedStatesQueuesIds);
                executionStateRepository.deleteByIds(finishedStateIds);
            }
        }
    }

    private void deleteOrphanExecutionQueues() {
        long timeLimitMillisOrphanExecutionQueues = System.currentTimeMillis() - ORPHAN_EXECUTION_QUEUES_INACTIVE_TIME;
        Set<Long> orphanExecutionQueuesIds = executionQueueRepository
                .getOrphanExecutionQueues(timeLimitMillisOrphanExecutionQueues);
        if (logger.isDebugEnabled()) {
            String orphanIds = orphanExecutionQueuesIds.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
            logger.debug("Will clean from queue the orphan amount: " + orphanExecutionQueuesIds.size());
            logger.debug("Will clean from queue the following orphans: " + orphanIds);
        }
        String orphanIds = orphanExecutionQueuesIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        logger.warn("Will clean from queue the orphan amount: " + orphanExecutionQueuesIds.size());
        logger.warn("Will clean from queue the following orphans: " + orphanIds);

        executionQueueRepository.deleteOrphanExecutionQueuesById(orphanExecutionQueuesIds);
    }
}