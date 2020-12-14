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

import io.cloudslang.score.events.*;
import io.cloudslang.worker.monitor.PerfMetricCollector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WorkerMetricCollectorServiceImplTest.MyTestConfig.class)
public class WorkerMetricCollectorServiceImplTest {
    private LinkedBlockingQueue<Map<MetricKeyValue, Serializable>> collectMetricQueue = new LinkedBlockingQueue<Map<MetricKeyValue, Serializable>>(10);

    @Autowired
    WorkerMetricCollectorService workerMetricCollectorService;
    @Autowired
    PerfMetricCollector perfMetricCollector;
    @Autowired
    @Qualifier("consumptionFastEventBus")
    private FastEventBus fastEventBus;


    @Test
    public void testWorkerMetricCollectorService() throws InterruptedException {
        Map<MetricKeyValue, Serializable> monitorInfo1 = new HashMap<>();
        Map<MetricKeyValue, Serializable> monitorInfo2 = new HashMap<>();
        collectMetricQueue.put(monitorInfo1);
        collectMetricQueue.put(monitorInfo2);
        workerMetricCollectorService.dispatchPerfMetric();
        verify(fastEventBus, times(1)).dispatch(anyObject());
    }

    @Configuration
    public static class MyTestConfig {

        @Bean
        public WorkerMetricCollectorService workerMetricCollectorService() {return new WorkerMetricCollectorServiceImpl();}
        @Bean
        public PerfMetricCollector perfMetricCollector() {return mock(PerfMetricCollector.class);}
        @Bean
        public FastEventBus consumptionFastEventBus() {return mock(FastEventBusImpl.class);}
    }
}
