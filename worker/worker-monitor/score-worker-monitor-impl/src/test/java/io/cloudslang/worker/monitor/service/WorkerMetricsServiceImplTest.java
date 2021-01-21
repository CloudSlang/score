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
import io.cloudslang.score.events.FastEventBus;
import io.cloudslang.score.events.FastEventBusImpl;
import io.cloudslang.worker.monitor.PerfMetricCollector;
import io.cloudslang.worker.monitor.metrics.*;
import javafx.util.Pair;
import org.junit.Assert;
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
@ContextConfiguration(classes = WorkerMetricsServiceImplTest.MyTestConfig.class)
public class WorkerMetricsServiceImplTest {
    private LinkedBlockingQueue<Map<WorkerPerformanceMetric, Serializable>> collectMetricQueue = new LinkedBlockingQueue<Map<WorkerPerformanceMetric, Serializable>>(10);

    @Autowired
    private WorkerMetricsService workerMetricsService;
    @Autowired
    private PerfMetricCollector perfMetricCollector;
    @Autowired
    private CpuUtilizationService cpuUtilizationService;
    @Autowired
    private MemoryUtilizationService memoryUtilizationService;
    @Autowired
    private EventBus eventBus;


    @Test
    public void testWorkerMetricCollectorService() throws InterruptedException {
        Map<WorkerPerformanceMetric, Serializable> monitorInfo1 = new HashMap<>();
        Map<WorkerPerformanceMetric, Serializable> monitorInfo2 = new HashMap<>();
        collectMetricQueue.put(monitorInfo1);
        collectMetricQueue.put(monitorInfo2);
        workerMetricsService.dispatchPerformanceMetrics();
        verify(eventBus, times(1)).dispatch(anyObject());
    }

    @Test
    public void testCpuPerfMetricCollectorService() throws InterruptedException {
        CpuUtilizationService cpuUtilizationService1 = new CpuUtilizationService();
        Pair<WorkerPerformanceMetric, Serializable> cpuInfo1 = new Pair<>(WorkerPerformanceMetric.CPU_USAGE,cpuUtilizationService1.getCurrentValue());
        double val = cpuUtilizationService1.getCurrentValue();
        System.out.println(val);
    }

    @Test
    public void testMemPerfMetricCollectorService() throws InterruptedException {
        MemoryUtilizationService memoryUtilizationService1 = new MemoryUtilizationService();
        Pair<WorkerPerformanceMetric, Serializable> cpuInfo1 = new Pair<>(WorkerPerformanceMetric.MEMORY_USAGE,memoryUtilizationService1.getCurrentValue());
        double val = memoryUtilizationService1.getCurrentValue();
        System.out.println(val);
//        when(cpuUtilizationService1.measure()).thenReturn(
    }

    @Configuration
    public static class MyTestConfig {

        @Bean
        public WorkerMetricsService workerMetricCollectorService() {return new WorkerMetricsServiceImpl();}
        @Bean
        public PerfMetricCollector perfMetricCollector() {return mock(PerfMetricCollector.class);}
        @Bean
        public EventBus eventBus() {return mock(EventBus.class);}
        @Bean
        public CpuUtilizationService cpuUtilizationService() {return mock(CpuUtilizationService.class);}
        @Bean
        public DiskReadUtilizationService diskReadUtilizationService() {return mock(DiskReadUtilizationService.class);}
        @Bean
        public DiskWriteUtilizationService diskWriteUtilizationService() {return mock(DiskWriteUtilizationService.class);}
        @Bean
        public MemoryUtilizationService memoryUtilizationService() {return mock(MemoryUtilizationService.class);}
//        @Bean
//        public HeapUtilizationService heapUtilizationService() {return mock(HeapUtilizationService.class);}
//        @Bean
//        public WorkerThreadUtilization workerThreadUtilization() {return mock(WorkerThreadUtilization.class);}

    }
}
