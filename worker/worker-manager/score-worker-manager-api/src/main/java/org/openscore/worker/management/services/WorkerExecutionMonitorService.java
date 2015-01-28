package org.openscore.worker.management.services;

public interface WorkerExecutionMonitorService {
    void collectMonitorInformation();
    void executeScheduledWorkerMonitors();
}
