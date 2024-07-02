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
package io.cloudslang.worker.monitor.metrics;

import io.cloudslang.worker.monitor.metric.WorkerPerfMetric;
import io.cloudslang.worker.monitor.service.WorkerPerformanceMetric;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Pair;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.software.os.OSProcess;

import java.io.Serializable;

import static io.cloudslang.worker.monitor.metric.WorkerPerfMetric.formatTo2Decimal;
import static io.cloudslang.worker.monitor.service.WorkerPerformanceMetric.CPU_USAGE;

public class CpuUtilizationService implements WorkerPerfMetric {

    private int cpuNumber;

    @PostConstruct
    public void init() {
        boolean isDisabled = Boolean.getBoolean("worker.monitoring.disable");
        if (!isDisabled) {
            SystemInfo systemInfo = new SystemInfo();
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            cpuNumber = processor.getLogicalProcessorCount();
        }
    }

    @Override
    public Pair<WorkerPerformanceMetric, Serializable> measure(OSProcess crtProcess, OSProcess oldProcess) {
        return Pair.of(CPU_USAGE, getCurrentValue(crtProcess, oldProcess));
    }

    public double getCurrentValue(OSProcess crtProcess, OSProcess oldProcess) {
        double cpuUsed = (crtProcess.getProcessCpuLoadBetweenTicks(oldProcess) * 100) / cpuNumber;
        return formatTo2Decimal(cpuUsed);
    }
}
