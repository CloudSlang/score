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

package io.cloudslang.engine.queue.services.cleaner;

import io.cloudslang.engine.queue.entities.ExecutionStatesData;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User:
 * Date: 14/10/13
 */
final public class QueueCleanerServiceImpl  implements QueueCleanerService {

    final private int BULK_SIZE = 500;

    @Autowired
   	private ExecutionQueueRepository executionQueueRepository;

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getNonLatestFinishedExecStateIds() {
        return executionQueueRepository.getNonLatestFinishedExecStateIds();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExecutionStatesData> getLatestExecutionStates() {
        return executionQueueRepository.getLatestExecutionStates();
    }

    @Transactional(readOnly = true)
    @Override
    public Set<Long> getExecutionStatesByFinishedMessageId(Set<Long> messageIds) {
        return executionQueueRepository.getExecutionStatesByFinishedMessageId(messageIds);
    }

    @Transactional(readOnly = true)
    @Override
    public Set<Long> getOrphanQueues(long cutOffTime) {
        return executionQueueRepository.getOrphanExecutionQueues(cutOffTime);
    }

    @Override
    @Transactional
    public void cleanFinishedSteps(Set<Long> toDeleteIds) {
        executionQueueRepository.deleteFinishedSteps(toDeleteIds);
    }

    @Transactional
    @Override
    public void cleanUnusedSteps(Set<Long> toDeleteIds) {
        executionQueueRepository.deleteUnusedSteps(toDeleteIds);
    }

    @Transactional
    @Override
    public void cleanOrphanQueues(Set<Long> toDeleteIds) {
        executionQueueRepository.deleteOrphanExecutionQueuesById(toDeleteIds);
    }
}
