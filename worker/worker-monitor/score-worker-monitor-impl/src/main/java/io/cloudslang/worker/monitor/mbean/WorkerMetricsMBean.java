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
package io.cloudslang.worker.monitor.mbean;

import io.cloudslang.worker.management.services.WorkerManager;
import io.cloudslang.worker.monitor.metrics.CpuUtilizationService;
import io.cloudslang.worker.monitor.metrics.DiskReadUtilizationService;
import io.cloudslang.worker.monitor.metrics.DiskWriteUtilizationService;
import io.cloudslang.worker.monitor.metrics.MemoryUtilizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import oshi.software.os.OSProcess;

import static io.cloudslang.worker.monitor.metric.WorkerPerfMetric.getProcess;

@ManagedResource(description = "Worker Metrics API")
public class WorkerMetricsMBean {

    @Autowired
    private CpuUtilizationService cpuUtilizationService;

    @Autowired
    private DiskReadUtilizationService diskReadUtilizationService;

    @Autowired
    private DiskWriteUtilizationService diskWriteUtilizationService;

    @Autowired
    private MemoryUtilizationService memoryUtilizationService;

    @Autowired
    private WorkerManager workerManager;

    @Autowired
    @Qualifier("numberOfExecutionThreads")
    private Integer numberOfThreads;

    private OSProcess prevCpuProcess;
    private OSProcess prevDiskReadProcess;
    private OSProcess prevDiskWriteProcess;

    public WorkerMetricsMBean() {
        this.prevCpuProcess = getProcess();
        this.prevDiskReadProcess = getProcess();
        this.prevDiskWriteProcess = getProcess();
    }

    @ManagedAttribute(description = "Current Cpu Usage")
    public double getCpuUsage() {
        OSProcess crtProcess = getProcess();
        try {
            return cpuUtilizationService.getCurrentValue(crtProcess, prevCpuProcess);
        } finally {
            prevCpuProcess = crtProcess;
        }
    }

    @ManagedAttribute(description = "Current Disk Read Usage")
    public long getDiskReadUsage() {
        OSProcess crtProcess = getProcess();
        try {
            return diskReadUtilizationService.getCurrentValue(crtProcess, prevDiskReadProcess);
        } finally {
            prevDiskReadProcess = crtProcess;
        }
    }

    @ManagedAttribute(description = "Current Disk Write Usage")
    public long getDiskWriteUsage() {
        OSProcess crtProcess = getProcess();
        try {
            return diskWriteUtilizationService.getCurrentValue(crtProcess, prevDiskWriteProcess);
        } finally {
            prevDiskWriteProcess = crtProcess;
        }
    }

    @ManagedAttribute(description = "Current Memory Usage")
    public double getMemoryUsage() {
        return memoryUtilizationService.getCurrentValue(getProcess());
    }

    @ManagedAttribute(description = "Running Tasks Count")
    public double getWorkerThreadsUsage() {
        return ((double) workerManager.getRunningTasksCount() * 100) / numberOfThreads;
    }

}