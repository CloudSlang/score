/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.worker.management.monitor;

import com.google.common.collect.Maps;
import io.cloudslang.worker.management.services.InBuffer;
import io.cloudslang.worker.management.services.OutboundBuffer;
import io.cloudslang.worker.management.services.WorkerManager;
import io.cloudslang.worker.management.services.WorkerMonitorInfoEnum;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class WorkerMonitorsImpl implements WorkerMonitors {
    private long monitorStartTime;
    @Autowired
    private WorkerManager workerManager;

    @Autowired
    private OutboundBuffer outBuffer;

    @Autowired
    private InBuffer inBuffer;

    @Autowired
    private List<WorkerMonitor> monitors;

    @PostConstruct
    public void init() {
        resetMonitor();
    }

    @Override
    public synchronized Map<WorkerMonitorInfoEnum, Serializable> getMonitorInfo() {
        try {
            Map<WorkerMonitorInfoEnum, Serializable> monitorInfo = Maps.newHashMap();

            Runtime runtime = Runtime.getRuntime();
            monitorInfo.put(WorkerMonitorInfoEnum.TOTAL_MEMORY, runtime.totalMemory());
            monitorInfo.put(WorkerMonitorInfoEnum.FREE_MEMORY, runtime.freeMemory());
            monitorInfo.put(WorkerMonitorInfoEnum.MAX_MEMORY, runtime.maxMemory());

            monitorInfo.put(WorkerMonitorInfoEnum.WORKER_ID, workerManager.getWorkerUuid());

            monitorInfo.put(WorkerMonitorInfoEnum.EXECUTION_THREADS_AMOUNT, workerManager.getExecutionThreadsCount());

            monitorInfo.put(WorkerMonitorInfoEnum.OUTBUFFER_CAPACITY, outBuffer.getCapacity());

            monitorInfo.put(WorkerMonitorInfoEnum.INBUFFER_CAPACITY, inBuffer.getCapacity());

            for (WorkerMonitor monitor : monitors) {
                monitor.captureMonitorInfo(monitorInfo);
            }

            monitorInfo.put(WorkerMonitorInfoEnum.MONITOR_START_TIME, monitorStartTime);
            monitorInfo.put(WorkerMonitorInfoEnum.MONITOR_END_TIME, System.currentTimeMillis());

            return monitorInfo;
        } finally {
            resetMonitor();
        }
    }

    private synchronized void resetMonitor() {
        monitorStartTime = System.currentTimeMillis();
    }
}
