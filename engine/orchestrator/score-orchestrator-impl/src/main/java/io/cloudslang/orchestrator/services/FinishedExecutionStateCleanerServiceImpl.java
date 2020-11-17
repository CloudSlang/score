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

import io.cloudslang.orchestrator.repositories.ExecutionStateRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import static io.cloudslang.score.facade.execution.ExecutionStatus.*;
import static java.util.Arrays.asList;
import static org.springframework.util.CollectionUtils.isEmpty;

public class FinishedExecutionStateCleanerServiceImpl implements FinishedExecutionStateCleanerService {

    private final int MAX_BULK_SIZE = Integer.getInteger("execution.state.clean.job.bulk.size", 200);
    private final int SPLIT_SIZE = 200;
    private static final long EXECUTION_STATE_INACTIVE_TIME = 30 * 60 * 1000L;

    private static final Logger logger = LogManager.getLogger(FinishedExecutionStateCleanerServiceImpl.class);

    @Autowired
    private ExecutionStateRepository executionStateRepository;

    @Override
    @Transactional
    public void cleanFinishedExecutionState() {
        try {
            cleanFinishedExecutionState(MAX_BULK_SIZE);
        } catch (Exception e) {
            logger.error("finished execution state cleaner job failed!", e);
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
}