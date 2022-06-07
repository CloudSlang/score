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
package io.cloudslang.worker.monitor;

import io.cloudslang.worker.management.services.WorkerManager;
import io.cloudslang.worker.monitor.metric.WorkerPerfMetric;
import io.cloudslang.worker.monitor.metrics.CpuUtilizationService;
import io.cloudslang.worker.monitor.metrics.DiskReadUtilizationService;
import io.cloudslang.worker.monitor.metrics.DiskWriteUtilizationService;
import io.cloudslang.worker.monitor.metrics.HeapUtilizationService;
import io.cloudslang.worker.monitor.metrics.MemoryUtilizationService;
import io.cloudslang.worker.monitor.metrics.WorkerThreadUtilization;
import io.cloudslang.worker.monitor.service.WorkerPerformanceMetric;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import oshi.software.os.OSProcess;
import java.lang.Boolean;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.cloudslang.worker.monitor.metric.WorkerPerfMetric.getProcess;

@Component
public class PerformanceMetricsCollector implements PerfMetricCollector {

    List<WorkerPerfMetric> workerPerfMetrics;

    @Autowired
    private WorkerManager workerManager;

    @Autowired
    private CpuUtilizationService cpuUtilizationService;

    @Autowired
    private MemoryUtilizationService memoryUtilizationService;

    @Autowired
    private DiskReadUtilizationService diskReadUtilizationService;

    @Autowired
    private DiskWriteUtilizationService diskWriteUtilizationService;

    @Autowired
    private WorkerThreadUtilization workerThreadUtilization;

    @Autowired
    private HeapUtilizationService heapUtilizationService;

    private OSProcess oldProcess;

    public PerformanceMetricsCollector() {
        boolean disabled = Boolean.getBoolean("worker.monitoring.disable");
        if (!disabled) {
            this.oldProcess = getProcess();
        }
    }

    @PostConstruct
    public void init() {
        createMetrics();
    }

    private void createMetrics() {
        workerPerfMetrics = new ArrayList<>(6);
        workerPerfMetrics.add(cpuUtilizationService);
        workerPerfMetrics.add(diskReadUtilizationService);
        workerPerfMetrics.add(memoryUtilizationService);
        workerPerfMetrics.add(heapUtilizationService);
        workerPerfMetrics.add(diskWriteUtilizationService);
        workerPerfMetrics.add(workerThreadUtilization);
    }

    @Override
    public Map<WorkerPerformanceMetric, Serializable> collectMetrics() {
        Map<WorkerPerformanceMetric, Serializable> currentValues = new HashMap<>(11);

        OSProcess crtProcess = getProcess();
        try {
            for (WorkerPerfMetric metric : workerPerfMetrics) {
                Pair<WorkerPerformanceMetric, Serializable> measurement = metric.measure(crtProcess, oldProcess);
                currentValues.put(measurement.getKey(), measurement.getValue());
            }
            currentValues.put(WorkerPerformanceMetric.WORKER_ID, workerManager.getWorkerUuid());
            currentValues.put(WorkerPerformanceMetric.WORKER_MEASURED_TIME, System.currentTimeMillis());
            return currentValues;
        } finally {
            oldProcess = crtProcess;
        }
    }

}
