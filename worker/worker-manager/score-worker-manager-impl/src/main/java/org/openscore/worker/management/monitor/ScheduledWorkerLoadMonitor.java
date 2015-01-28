package org.openscore.worker.management.monitor;

import org.openscore.worker.management.services.OutboundBuffer;
import org.openscore.worker.management.services.WorkerManager;
import org.openscore.worker.management.services.WorkerMonitorInfoEnum;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.HashMap;

public class ScheduledWorkerLoadMonitor implements ScheduledWorkerMonitor{
    @Autowired
    private WorkerManager workerManager;

    @Autowired
    private OutboundBuffer outBuffer;

    private int probeCount;
    private int inBufferSize;
    private int outBufferSize;
    private int runningTasks;

    @PostConstruct
    public void init() {
        resetMonitor();
    }

    @Override
    public synchronized void executeScheduled() {
        probeCount++;
        inBufferSize += workerManager.getInBufferSize();
        outBufferSize += outBuffer.getWeight();
        runningTasks += workerManager.getRunningTasksCount();
    }

    @Override
    public synchronized void captureMonitorInfo(HashMap<WorkerMonitorInfoEnum, Serializable> monitorInfo) {
        monitorInfo.put(WorkerMonitorInfoEnum.INBUFFER_SIZE_AVERAGE, probeCount > 0 ? inBufferSize / probeCount : 0);
        monitorInfo.put(WorkerMonitorInfoEnum.OUTBUDDER_SIZE_AVERAGE, probeCount > 0 ? outBufferSize / probeCount : 0);
        monitorInfo.put(WorkerMonitorInfoEnum.RUNNING_TASKS_AVERAGE, probeCount > 0 ? runningTasks / probeCount : 0);
        resetMonitor();
    }

    private void resetMonitor() {
        probeCount = 0;
        inBufferSize = 0;
        outBufferSize = 0;
        runningTasks = 0;
    }
}
