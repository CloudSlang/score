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

import io.cloudslang.orchestrator.repositories.SuspendedExecutionsRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;


public class SuspendedExecutionCleanerServiceImpl implements SuspendedExecutionCleanerService {

    private final int MAX_BULK_SIZE = Integer.getInteger("suspendedexecution.job.bulk.size", 200);
    private final int SPLIT_SIZE = 200;

    @Autowired
    private SuspendedExecutionsRepository suspendedExecutionsRepository;

    private static final Logger logger = Logger.getLogger(SuspendedExecutionCleanerServiceImpl.class);

    @Override
    @Transactional
    public void cleanupSuspendedExecutions() {
        try {
            cleanupSuspendedExecutions(MAX_BULK_SIZE);
        } catch (Exception e) {
            logger.error("suspended execution cleaner job failed!", e);
        }
    }

    private void cleanupSuspendedExecutions(Integer bulkSize) {
        for (int i = 1; i <= bulkSize / SPLIT_SIZE; i++) {
            Collection<String> toBeDeleted = suspendedExecutionsRepository.collectCompletedSuspendedExecutions(new PageRequest(0, SPLIT_SIZE));
            if (!CollectionUtils.isEmpty(toBeDeleted)) {
                suspendedExecutionsRepository.deleteByIds(toBeDeleted);
            }
        }
    }
}

