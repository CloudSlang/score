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
    private MemoryUtilizationService memoryUtilizationService;

    @Autowired
    private DiskWriteUtilizationService diskWriteUtilizationService;

    @Autowired
    private WorkerManager workerManager;

    @Autowired
    @Qualifier("numberOfExecutionThreads")
    private Integer numberOfThreads;

    private OSProcess oldProcess;

    public WorkerMetricsMBean() {
        this.oldProcess = getProcess();
    }

    @ManagedAttribute(description = "Current Cpu Usage")
    public double getCpuUsage() {
        OSProcess process = getProcess();
        try {
            return cpuUtilizationService.getCurrentValue(process, oldProcess);
        } finally {
            oldProcess = process;
        }

    }

    @ManagedAttribute(description = "Current Memory Usage")
    public double getMemoryUsage() {
        OSProcess process = getProcess();
        try {
            return memoryUtilizationService.getCurrentValue(process);
        } finally {
            oldProcess = process;
        }
    }

    @ManagedAttribute(description = "Current Disk Read Usage")
    public long getDiskReadUsage() {
        OSProcess process = getProcess();
        try {
            return diskReadUtilizationService.getCurrentValue(process, oldProcess);
        } finally {
            oldProcess = process;
        }

    }

    @ManagedAttribute(description = "Current Disk Write Usage")
    public long getDiskWriteUsage() {
        OSProcess process = getProcess();
        try {
            return diskWriteUtilizationService.getCurrentValue(process, oldProcess);
        } finally {
            oldProcess = process;
        }

    }

    @ManagedAttribute(description = "Running Tasks Count")
    public double getWorkerThreadsUsage() {
        return ((double) workerManager.getRunningTasksCount() * 100) / numberOfThreads;
    }

}