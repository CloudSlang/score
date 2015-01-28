package org.openscore.worker.management.monitor;

import org.openscore.worker.management.services.WorkerMonitorInfoEnum;

import java.io.Serializable;
import java.util.HashMap;

public interface WorkerMonitor {
    void captureMonitorInfo(HashMap<WorkerMonitorInfoEnum, Serializable> monitorInfo);
}
