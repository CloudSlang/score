package io.cloudslang.engine.queue.services;

import io.cloudslang.engine.queue.entities.ExecStatus;
import io.cloudslang.engine.queue.repositories.ExecutionQueueRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


public class BusyWorkersServiceImpl implements BusyWorkersService {

    private final Logger logger = Logger.getLogger(BusyWorkersServiceImpl.class);
    private List<String> busyWorkers = new ArrayList<>();

    @Autowired
    private ExecutionQueueRepository executionQueueRepository;

    @Override
    @Transactional(readOnly = true)
    public boolean isWorkerBusy(String workerId) {
        return busyWorkers.contains(workerId);
    }
    @Override
    @Transactional(readOnly = true)
    public void findBusyWorkers() {
        long startTime = 0;
        if (logger.isDebugEnabled()) {
            startTime = System.currentTimeMillis();
            logger.debug("Querying for busy workers...");
        }
        busyWorkers = executionQueueRepository.getBusyWorkers(ExecStatus.ASSIGNED);

        if (logger.isDebugEnabled()) {
            long endTime = System.currentTimeMillis();
            logger.debug("Queried for busy workers, the following workers are busy: " + this.busyWorkers + ". Query took: " + (endTime - startTime) + " ms to complete");
        }
    }
}
