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
package io.cloudslang.worker.management.monitor;

import io.cloudslang.worker.management.services.OutboundBuffer;
import io.cloudslang.worker.management.services.WorkerManager;
import io.cloudslang.worker.management.services.WorkerMonitorInfoEnum;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;

public class ScheduledWorkerLoadMonitor implements ScheduledWorkerMonitor{
    @Autowired
    private WorkerManager workerManager;

    @Autowired
    private OutboundBuffer outBuffer;

    private int probeCount = 0;
    private int inBufferSize = 0;
    private int outBufferSize = 0;
    private int runningTasks = 0;

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
    public synchronized void captureMonitorInfo(Map<WorkerMonitorInfoEnum, Serializable> monitorInfo) {
        monitorInfo.put(WorkerMonitorInfoEnum.INBUFFER_SIZE_AVERAGE, probeCount > 0 ? inBufferSize / probeCount : 0);
        monitorInfo.put(WorkerMonitorInfoEnum.OUTBUFFER_SIZE_AVERAGE, probeCount > 0 ? outBufferSize / probeCount : 0);
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
