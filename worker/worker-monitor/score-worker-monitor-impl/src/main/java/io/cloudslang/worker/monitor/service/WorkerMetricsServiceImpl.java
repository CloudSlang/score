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
package io.cloudslang.worker.monitor.service;

import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.worker.management.monitor.WorkerStateUpdateService;
import io.cloudslang.worker.monitor.PerfMetricCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static io.cloudslang.score.events.EventConstants.WORKER_PERFORMANCE_MONITOR;
import static java.lang.Boolean.getBoolean;
import static java.lang.Integer.getInteger;

public class WorkerMetricsServiceImpl implements WorkerMetricsService {
    private static final Logger logger = LogManager.getLogger(WorkerMetricsServiceImpl.class);
    private static final int capacity = getInteger("metrics.collection.sampleCount", 10);
    private static final boolean disabled = getBoolean("worker.monitoring.disable");

    @Autowired
    private PerfMetricCollector perfMetricCollector;

    @Autowired
    private WorkerStateUpdateService workerStateUpdateService;

    private final LinkedBlockingQueue<Map<WorkerPerformanceMetric, Serializable>> collectMetricQueue =
            new LinkedBlockingQueue<>(capacity);

    @Autowired
    private EventBus eventBus;

    @Override
    public void collectPerformanceMetrics() {
        try {
            if (!isMonitoringDisabled()) {
                Map<WorkerPerformanceMetric, Serializable> metricInfo = perfMetricCollector.collectMetrics();
                collectMetricQueue.put(metricInfo);
            }
        } catch (Exception e) {
            logger.error("Failed to compute metric or collect metrics: ", e);
        }
    }

    @Override
    public void dispatchPerformanceMetrics() {
        try {
            if (!isMonitoringDisabled()) {
                ArrayList<Map<WorkerPerformanceMetric, Serializable>> metricList = new ArrayList<>(collectMetricQueue.size());
                collectMetricQueue.drainTo(metricList);
                // Dispatch the event
                eventBus.dispatch(new ScoreEvent(WORKER_PERFORMANCE_MONITOR, metricList));
            }
        } catch (Exception e) {
            logger.error("Failed to dispatch metric info event", e);
        }
    }

    private boolean isMonitoringDisabled() {
        return disabled || workerStateUpdateService.isMonitoringDisabled();
    }

}
