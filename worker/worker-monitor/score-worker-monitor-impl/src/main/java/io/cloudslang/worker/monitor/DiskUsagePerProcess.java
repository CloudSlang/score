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

import io.cloudslang.worker.monitor.service.MetricKeyValue;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DiskUsagePerProcess implements WorkerPerfMetric {

    @Override
    public Map<MetricKeyValue, Serializable> measure() {
        Map<MetricKeyValue, Serializable> diskUsage = new HashMap<>();
        diskUsage.put(MetricKeyValue.DISK_USAGE, getCurrentValue());
        return diskUsage;
    }

    public long getCurrentValue() {
        OSProcess process;
        SystemInfo si = new SystemInfo();
        OperatingSystem os = si.getOperatingSystem();
        int processId = os.getProcessId(); //current process id
        process = os.getProcess(processId);
        long readBytes = process.getBytesRead();
        return readBytes;
    }
}
