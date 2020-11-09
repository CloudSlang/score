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

import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public Set<Long> getFinishedExecStateIds() {
        return executionQueueRepository.getFinishedExecStateIds();
    }

    @Override
    @Transactional
    public void cleanFinishedSteps(Set<Long> ids) {
        executionQueueRepository.deleteFinishedSteps(ids);
    }

    @Override
    @Transactional
    public Set<Long> getFlowCompletedExecStateIds() {
        return executionQueueRepository.getFlowCompletedExecStateIds();
    }

    @Override
    @Transactional
    public int deleteOrphanSteps() {
        return executionQueueRepository.deleteOrphanSteps();
    }

}
