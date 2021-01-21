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
import io.cloudslang.worker.monitor.metrics.*;
import io.cloudslang.worker.monitor.service.WorkerPerformanceMetric;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;

@Component
public class PerformanceMetricsCollector implements PerfMetricCollector {

    @Autowired
    private WorkerManager workerManager;

    @Autowired
    @Qualifier("numberOfExecutionThreads")
    private int numberOfThreads;

    List<WorkerPerfMetric> workerPerfMetrics;

    @PostConstruct
    public void init() {
        IntSupplier runningTaskCount = () -> workerManager.getRunningTasksCount();
        workerPerfMetrics.add(new WorkerThreadUtilization(runningTaskCount,numberOfThreads));
    }

    public PerformanceMetricsCollector() { createMetrics(); }

    private void createMetrics() {
        workerPerfMetrics = new ArrayList<>();
        workerPerfMetrics.add(new CpuUtilizationService());
        workerPerfMetrics.add(new DiskReadUtilizationService());
        workerPerfMetrics.add(new MemoryUtilizationService());
        workerPerfMetrics.add(new HeapUtilizationService());
        workerPerfMetrics.add(new DiskWriteUtilizationService());
    }

    @Override
    public Map<WorkerPerformanceMetric, Serializable> collectMetrics() {
        Map<WorkerPerformanceMetric, Serializable> currentValues = new HashMap<>();
        for (WorkerPerfMetric metric :
                workerPerfMetrics) {
            Pair<WorkerPerformanceMetric, Serializable> currentPair = metric.measure();
            currentValues.put(currentPair.getKey(),currentPair.getValue());
        }
        currentValues.put(WorkerPerformanceMetric.WORKER_ID,workerManager.getWorkerUuid());
        currentValues.put(WorkerPerformanceMetric.WORKER_MEASURED_TIME,System.currentTimeMillis());
        return currentValues;
    }
}
