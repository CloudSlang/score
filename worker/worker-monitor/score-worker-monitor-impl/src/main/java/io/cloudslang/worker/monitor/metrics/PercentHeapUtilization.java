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
import io.cloudslang.worker.monitor.service.MetricKeyValue;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class PercentHeapUtilization extends WorkerPerfMetricImpl {
    @Override
    public Map<MetricKeyValue, Serializable> measure() {
        Map<MetricKeyValue, Serializable> heapUsage = new HashMap<>();
        heapUsage.put(MetricKeyValue.HEAP_SIZE, getCurrentValue());
        return heapUsage;
    }

    public double getCurrentValue() {
        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();
        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        double percentageHeapUsed = ((double) heapSize / (double) heapMaxSize) * 100;
        return formatTo2Decimal(percentageHeapUsed);
    }


}
