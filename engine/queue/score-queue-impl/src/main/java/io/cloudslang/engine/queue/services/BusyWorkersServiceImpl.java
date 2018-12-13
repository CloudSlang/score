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

package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BusyWorkersServiceImpl implements BusyWorkersService {

    private final Logger logger = Logger.getLogger(BusyWorkersServiceImpl.class);
    private Map<String, String> busyWorkersMap = new ConcurrentHashMap<>();

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isWorkerBusy(String workerId) {
        return busyWorkersMap.containsKey(workerId);
    }

    @Override
    @Transactional(readOnly = true)
    public void findBusyWorkers() {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        List<String> busyWorkers = executionQueueRepository.getBusyWorkers(ExecStatus.ASSIGNED);
        this.busyWorkersMap.clear();
        for (String bw : busyWorkers) {
            this.busyWorkersMap.put(bw, bw);
        }
        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger.debug("Queried for busy workers, the following workers are busy: " + this.busyWorkersMap + ". Query took: " + (endTime - startTime) + " ms to complete");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void clearBusyWorkers() {
        busyWorkersMap.clear();
    }


}
