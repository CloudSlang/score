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

import com.google.common.collect.Lists;
import io.cloudslang.engine.queue.entities.ExecutionStatesData;
import io.cloudslang.engine.queue.services.cleaner.QueueCleanerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.cloudslang.score.facade.execution.ExecutionStatus.*;

public class ExecutionCleanerServiceImpl implements ExecutionCleanerService {

    private static final Logger logger = LogManager.getLogger(ExecutionCleanerServiceImpl.class);

    private static final int MAX_BULK_SIZE = Integer.getInteger("execution.state.clean.job.bulk.size", 200);
    private static final int SPLIT_SIZE = 50;

    private static final long EXECUTION_STATE_INACTIVE_TIME_FINISHED = Duration.ofHours(3).toMillis();
    private static final long EXECUTION_STATE_INACTIVE_TIME_CANCELED = Duration.ofHours(24).toMillis();
    private static final long ORPHAN_EXECUTION_QUEUES_INACTIVE_TIME = Duration.ofHours(72).toMillis();


    @Autowired
    private ExecutionStateService executionStateService;

    @Autowired
    private QueueCleanerService queueCleanerService;

    @Override
    public void cleanExecutions() {
        try {
            performExecutionCleanup();
        } catch (Exception exception) {
            logger.error("Execution cleanup job failed: ", exception);
        }
    }

    private void performExecutionCleanup() {
        // Safely delete execution queues and state mappings for non-latest or unused states
        // from oo_execution_states and oo_execution_queues
        deleteUnusedExecutionStatesAndQueues();

        // Remove all finished executions and related data (CANCELED, COMPLETED, SYSTEM_FAILURE)
        // from oo_execution_queues, oo_execs_states_execs_mappings, oo_execution_states and oo_execution_state
        deleteFinishedExecutionData();

        // Clean up orphaned queue entries left from parallel or multi-instance execution from oo_execution_queues
        deleteOrphanExecutionQueues();
    }

    private void deleteUnusedExecutionStatesAndQueues() {
        try {
            Set<Long> ids = queueCleanerService.getNonLatestFinishedExecStateIds();
            if (logger.isDebugEnabled()) {
                logger.debug("Detected {} unused entries to clean from oo_execution_states and oo_execution_queues",
                        ids.size());
            }

            List<ExecutionStatesData> latestExecutionStateData = queueCleanerService.getLatestExecutionStates();
            if (logger.isDebugEnabled()) {
                String execStates = latestExecutionStateData.stream()
                        .map(ExecutionStatesData::toString)
                        .collect(Collectors.joining(", "));
                logger.debug("Latest execution states before unused execution cleaning job: {}", execStates);
            }

            List<Long> idList = new ArrayList<>(ids);
            List<List<Long>> partitions = Lists.partition(idList, SPLIT_SIZE);

            int processedEntries = 0;
            for (List<Long> partition : partitions) {
                if (processedEntries >= MAX_BULK_SIZE) {
                    break;
                }
                queueCleanerService.cleanUnusedSteps(new HashSet<>(partition));
                processedEntries += partition.size();
            }
        } catch (Exception exception) {
            logger.error("Unused executions cleanup job failed: ", exception);
        }
    }

    private void deleteFinishedExecutionData() {
        try {
            long timeLimitMillisFinished = System.currentTimeMillis() - EXECUTION_STATE_INACTIVE_TIME_FINISHED;
            long timeLimitMillisCanceled = System.currentTimeMillis() - EXECUTION_STATE_INACTIVE_TIME_CANCELED;

            for (int i = 0; i <= MAX_BULK_SIZE / SPLIT_SIZE; i++) {
                PageRequest pageRequest = PageRequest.of(0, SPLIT_SIZE);

                List<Long> finishedStateIds = executionStateService
                        .findExecutionStateByStatusInAndUpdateTimeLessThanEqual(
                                Arrays.asList(COMPLETED, SYSTEM_FAILURE),
                                timeLimitMillisFinished,
                                pageRequest
                        );

                List<Long> canceledStateIds = executionStateService
                        .findExecutionStateByStatusInAndUpdateTimeLessThanEqual(
                                Collections.singletonList(CANCELED),
                                timeLimitMillisCanceled,
                                pageRequest
                        );


                if (logger.isDebugEnabled()) {
                    logger.debug("Detected {} completed entries and {} canceled entries to clean " +
                                    "from oo_execution_queues, oo_execs_states_execs_mappings, oo_execution_states and oo_execution_state ",
                            finishedStateIds.size(), canceledStateIds.size());
                }

                if (finishedStateIds.size() + canceledStateIds.size() != 0) {
                    List<Long> allFinishedAndCanceledIds = new ArrayList<>(finishedStateIds.size() + canceledStateIds.size());
                    allFinishedAndCanceledIds.addAll(finishedStateIds);
                    allFinishedAndCanceledIds.addAll(canceledStateIds);
                    Set<Long> finishedAndCanceledStatesQueuesIds = queueCleanerService
                            .getExecutionStatesByFinishedMessageId(new HashSet<>(allFinishedAndCanceledIds));

                    queueCleanerService.cleanFinishedSteps(finishedAndCanceledStatesQueuesIds);
                    executionStateService.deleteExecutionStateByIds(allFinishedAndCanceledIds);
                }
            }
        } catch (Exception exception) {
            logger.error("Finished execution cleanup job failed: ", exception);
        }
    }

    private void deleteOrphanExecutionQueues() {
        try {
            long timeLimitMillisOrphanExecutionQueues = System.currentTimeMillis() - ORPHAN_EXECUTION_QUEUES_INACTIVE_TIME;
            Set<Long> orphanExecutionQueuesIds = queueCleanerService
                    .getOrphanQueues(timeLimitMillisOrphanExecutionQueues);
            if (logger.isDebugEnabled()) {
                String orphanIds = orphanExecutionQueuesIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", "));
                logger.debug("Detected {} orphan execution queue entries to clean: {}",
                        orphanExecutionQueuesIds.size(), orphanIds);

            }

            queueCleanerService.cleanOrphanQueues(orphanExecutionQueuesIds);
        } catch (Exception exception) {
            logger.error("Orphan execution queues cleanup job failed: ", exception);
        }
    }
}