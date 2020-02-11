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

import static org.springframework.util.CollectionUtils.isEmpty;

import io.cloudslang.orchestrator.repositories.FinishedBranchRepository;
import java.util.List;
import javax.transaction.Transactional;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

public class FinishedBranchesCleanerServiceImpl implements FinishedBranchesCleanerService {

    private final int MAX_BULK_SIZE = Integer.getInteger("suspendedexecution.job.bulk.size", 250);
    private final int SPLIT_SIZE = 250;

    private static final Logger logger = Logger.getLogger(FinishedBranchesCleanerServiceImpl.class);

    @Autowired
    private FinishedBranchRepository finishedBranchRepository;

    @Override
    @Transactional
    public void cleanFinishedBranches() {
        try {
            cleanFinishedBranches(MAX_BULK_SIZE);
        } catch (Exception e) {
            logger.error("finished branches cleaner job failed!", e);
        }
    }

    private void cleanFinishedBranches(Integer bulkSize) {
        for (int i = 1; i <= bulkSize / SPLIT_SIZE; i++) {
            List<Long> toBeDeleted = finishedBranchRepository.collectOrphanFinishedBranches(new PageRequest(0, SPLIT_SIZE));
            if (!isEmpty(toBeDeleted)) {
                finishedBranchRepository.deleteByIds(toBeDeleted);
            }
        }
    }
}
