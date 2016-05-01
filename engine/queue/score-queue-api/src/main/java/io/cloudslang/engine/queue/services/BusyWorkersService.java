package io.cloudslang.engine.queue.services;

public interface BusyWorkersService {
    boolean isWorkerBusy(String workerId);
    void findBusyWorkers();
}
