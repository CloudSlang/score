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

import io.cloudslang.worker.monitor.service.MetricKeyValue;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PercentCPUByProcess extends WorkerPerfMetricImpl {
    private static OSProcess oldProcess;
    @Override
    public Map<MetricKeyValue, Serializable> measure() {
        Map<MetricKeyValue, Serializable> cpuUsage = new HashMap<>();
        cpuUsage.put(MetricKeyValue.CPU_USAGE, getCurrentValue());
        return cpuUsage;
    }

    public double getCurrentValue() {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        int cpuNumber = processor.getLogicalProcessorCount();
        int pid = getCurrentProcessId();
        oldProcess = operatingSystem.getProcess(pid);
        OSProcess osProcess = operatingSystem.getProcess(pid);
        double cpuUsed = (osProcess.getProcessCpuLoadBetweenTicks(oldProcess) * 100) / cpuNumber;
        oldProcess = osProcess;
        return formatTo2Decimal(cpuUsed);
    }
}
