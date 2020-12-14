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

import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.FastEventBus;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.worker.monitor.PerfMetricCollector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class WorkerMetricCollectorServiceImpl implements WorkerMetricCollectorService {
    protected static final Logger logger = LogManager.getLogger(WorkerMetricCollectorServiceImpl.class);
    static int capacity = Integer.parseInt(System.getProperty("Number.Of.Samples.To.Collect", "10"));
    private LinkedBlockingQueue<Map<MetricKeyValue, Serializable>> collectMetricQueue = new LinkedBlockingQueue<Map<MetricKeyValue, Serializable>>(capacity);
    @Autowired
    PerfMetricCollector perfMetricCollector;
    @Autowired
    @Qualifier("consumptionFastEventBus")
    private FastEventBus fastEventBus;
    private final ReentrantLock reLock = new ReentrantLock();

    @Override
    public void collectPerfMetrics() {
        if (logger.isDebugEnabled()) {
            logger.debug("Collecting Worker Metrics");
        }
        while (reLock.isLocked());
        try {
            Map<MetricKeyValue, Serializable> metricInfo = perfMetricCollector.collectMetric();
            collectMetricQueue.put(metricInfo);
            if (logger.isDebugEnabled()) {
                logger.debug("Sending Worker Metric Info:[" + metricInfo + "]");
            }
        } catch (Exception e) {
            logger.error("Failed to load metric into queue", e);
        }
    }

    @Override
    public void dispatchPerfMetric() {
        reLock.lock();
        try {
            Map<Integer,Map<MetricKeyValue, Serializable>> metricData = convertQueueToHashMap(collectMetricQueue);
            ScoreEvent scoreEvent = new ScoreEvent(EventConstants.WORKER_PERFORMANCE_MONITOR, (Serializable) metricData);
            fastEventBus.dispatch(scoreEvent);
            collectMetricQueue.clear();
        } catch (Exception e) {
            logger.error("Failed to dispatch metric info event", e);
        } finally {
            reLock.unlock();
        }
    }
    private Map<Integer,Map<MetricKeyValue, Serializable>> convertQueueToHashMap(LinkedBlockingQueue<Map<MetricKeyValue, Serializable>> metricQueue) {
        Map<Integer,Map<MetricKeyValue, Serializable>> metricData = new HashMap<>();
        for(int i=0;i<metricQueue.size();i++) {
            metricData.put(i,metricQueue.peek());
        }
        return metricData;
    }
}
