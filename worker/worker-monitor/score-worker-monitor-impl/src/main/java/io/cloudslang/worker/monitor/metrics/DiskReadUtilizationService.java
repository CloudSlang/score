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
import org.apache.commons.lang3.tuple.Pair;
import oshi.software.os.OSProcess;

import java.io.Serializable;

import static io.cloudslang.worker.monitor.service.WorkerPerformanceMetric.DISK_READ_USAGE;

public class DiskReadUtilizationService implements WorkerPerfMetric {

    @Override
    public Pair<WorkerPerformanceMetric, Serializable> measure(OSProcess crtProcess, OSProcess oldProcess) {
        return Pair.of(DISK_READ_USAGE, getCurrentValue(crtProcess, oldProcess));
    }

    public long getCurrentValue(OSProcess crtProcess, OSProcess oldProcess) {
        return crtProcess.getBytesRead() - ((oldProcess != null) ? oldProcess.getBytesRead() : 0);
    }
}