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
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.worker.monitor.PerfMetricCollector;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Map;


@Service
public class WorkerMetricCollectorServiceImpl implements WorkerMetricCollectorService {
    protected static final Logger logger = Logger.getLogger(WorkerMetricCollectorServiceImpl.class);
    @Autowired
    PerfMetricCollector perfMetricCollector;
    @Autowired
    private EventBus eventBus;

    @Override
    public void collectPerfMetrics() {
        if (logger.isDebugEnabled()) {
            logger.debug("Collecting Worker Metrics");
        }
        try {
            Map<MetricKeyValue, Serializable> monitorInfo = perfMetricCollector.collectMetric();
            if (logger.isDebugEnabled()) {
                logger.debug("Sending Worker Monitors Info:[" + monitorInfo + "]");
            }
            ScoreEvent event = new ScoreEvent(EventConstants.WORKER_PERFORMANCE_MONITOR, (Serializable) monitorInfo);
            eventBus.dispatch(event);
        } catch (InterruptedException e) {
            logger.error("Failed to dispatch monitor info event", e);
        }
    }
}