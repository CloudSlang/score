package org.openscore.worker.management.monitor;

public interface ScheduledWorkerMonitor extends WorkerMonitor{
    void executeScheduled();
}
