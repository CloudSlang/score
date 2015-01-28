package org.openscore.worker.management.monitor;

import org.openscore.worker.management.services.InBuffer;
import org.openscore.worker.management.services.OutboundBuffer;
import org.openscore.worker.management.services.WorkerManager;
import org.openscore.worker.management.services.WorkerMonitorInfoEnum;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class WorkerMonitorsImpl implements WorkerMonitors {
    private long monitorStartTime;
    @Autowired
    private WorkerManager workerManager;

    @Autowired
    private OutboundBuffer outBuffer;

    @Autowired
    private InBuffer inBuffer;

    @Autowired
    List<WorkerMonitor> monitors;

    @PostConstruct
    public void init() {
        resetMonitor();
    }

    @Override
    public synchronized HashMap<WorkerMonitorInfoEnum, Serializable> getMonitorInfo() {
        HashMap<WorkerMonitorInfoEnum, Serializable> monitorInfo = new HashMap<>();

        Runtime runtime = Runtime.getRuntime();
        monitorInfo.put(WorkerMonitorInfoEnum.TOTAL_MEMORY, runtime.totalMemory());
        monitorInfo.put(WorkerMonitorInfoEnum.FREE_MOMORY, runtime.freeMemory());
        monitorInfo.put(WorkerMonitorInfoEnum.MAX_MOMORY, runtime.maxMemory());

        monitorInfo.put(WorkerMonitorInfoEnum.WROKER_ID, workerManager.getWorkerUuid());

        monitorInfo.put(WorkerMonitorInfoEnum.EXECUTION_THREADS_AMOUNT, workerManager.getExecutionThreadsCount());

        monitorInfo.put(WorkerMonitorInfoEnum.OUTBUFFER_CAPACITY, outBuffer.getCapacity());

        monitorInfo.put(WorkerMonitorInfoEnum.INBUFFER_CAPACITY, inBuffer.getCapacity());

        for (WorkerMonitor monitor : monitors) {
            monitor.captureMonitorInfo(monitorInfo);
        }

        monitorInfo.put(WorkerMonitorInfoEnum.MONITOR_START_TIME, monitorStartTime);
        monitorInfo.put(WorkerMonitorInfoEnum.MONITOR_END_TIME, System.currentTimeMillis());

        resetMonitor();
        return monitorInfo;
    }

    private synchronized void resetMonitor() {
        monitorStartTime = System.currentTimeMillis();
    }
}
