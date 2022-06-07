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
package io.cloudslang.worker.monitor.metric;

import io.cloudslang.worker.monitor.service.WorkerPerformanceMetric;
import org.apache.commons.lang3.tuple.Pair;
import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.util.Objects.requireNonNull;

public interface WorkerPerfMetric {
    Logger logger = LogManager.getLogger(WorkerPerfMetric.class);
    Pair<WorkerPerformanceMetric, Serializable> measure(OSProcess crtProcess, OSProcess oldProcess);

    static double formatTo2Decimal(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
    }

    static OSProcess getProcess() {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem operatingSystem = systemInfo.getOperatingSystem();
        int processId = operatingSystem.getProcessId();
        logger.debug("Process id is " + processId);
        return requireNonNull(operatingSystem.getProcess(processId), "OSProcess is null");
    }
}
