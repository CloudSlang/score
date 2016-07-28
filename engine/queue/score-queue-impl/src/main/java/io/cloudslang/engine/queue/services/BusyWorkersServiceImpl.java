/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 * <p/>
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *******************************************************************************/

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
        boolean busyWorkerContained = busyWorkersMap.containsKey(workerId);
        return busyWorkerContained;
    }

    @Override
    @Transactional(readOnly = true)
    public void findBusyWorkers() {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
        }

        List<String> busyWorkers = executionQueueRepository.getBusyWorkers(ExecStatus.ASSIGNED);
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
