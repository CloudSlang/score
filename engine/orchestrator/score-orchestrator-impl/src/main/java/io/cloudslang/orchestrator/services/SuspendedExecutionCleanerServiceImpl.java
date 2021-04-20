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

import io.cloudslang.orchestrator.entities.SuspendedExecution;
import io.cloudslang.orchestrator.repositories.FinishedBranchRepository;
import io.cloudslang.orchestrator.repositories.SuspendedExecutionsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.MULTI_INSTANCE;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.NON_BLOCKING;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.PARALLEL;
import static io.cloudslang.orchestrator.enums.SuspendedExecutionReason.PARALLEL_LOOP;
import static java.util.EnumSet.of;
import static org.springframework.util.CollectionUtils.isEmpty;
import static java.util.stream.Collectors.toSet;


@Component
public class SuspendedExecutionCleanerServiceImpl implements SuspendedExecutionCleanerService {

    private final int MAX_BULK_SIZE = Integer.getInteger("suspendedexecution.job.bulk.size", 200);
    private final int SPLIT_SIZE = 200;

    @Autowired
    private SuspendedExecutionsRepository suspendedExecutionsRepository;

    @Autowired
    private FinishedBranchRepository finishedBranchRepository;

    private static final Logger logger = LogManager.getLogger(SuspendedExecutionCleanerServiceImpl.class);

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanupSuspendedExecutions() {
        try {
            cleanupSuspendedExecutions(MAX_BULK_SIZE);
        } catch (Exception e) {
            logger.error("suspended execution cleaner job failed!", e);
        }
    }

    private void cleanupSuspendedExecutions(Integer bulkSize) {
        for (int i = 1; i <= bulkSize / SPLIT_SIZE; i++) {
            PageRequest pageRequest = PageRequest.of(0, SPLIT_SIZE);
            List<SuspendedExecution> toBeDeleted = suspendedExecutionsRepository.findFinishedSuspendedExecutions(
                    of(PARALLEL, NON_BLOCKING, PARALLEL_LOOP, MULTI_INSTANCE), pageRequest);
            if (!isEmpty(toBeDeleted)) {
                suspendedExecutionsRepository.deleteByIds(toBeDeleted.stream().map(SuspendedExecution::getExecutionId)
                        .collect(toSet()));
                finishedBranchRepository.deleteByIds(toBeDeleted.stream().map(x -> Long.valueOf(x.getExecutionId()))
                        .collect(toSet()));
            }
        }
    }
}

