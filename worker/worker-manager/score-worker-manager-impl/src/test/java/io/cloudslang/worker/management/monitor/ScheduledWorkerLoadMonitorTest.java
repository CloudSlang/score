/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.worker.management.monitor;

import static org.junit.Assert.*;

import io.cloudslang.orchestrator.services.EngineVersionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.cloudslang.engine.node.services.WorkerNodeService;
import io.cloudslang.worker.management.WorkerConfigurationService;
import io.cloudslang.worker.management.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = ScheduledWorkerLoadMonitorTest.MyTestConfig.class)
public class ScheduledWorkerLoadMonitorTest {
    @Autowired
    WorkerManager workerManager;

    @Autowired
    OutboundBuffer outboundBuffer;

    @Autowired
    ScheduledWorkerLoadMonitor monitor;

    @Before
    public void setUp() {
        reset(workerManager, outboundBuffer);
    }

    @Test
    public void testLoadMonitor() {
        when(workerManager.getInBufferSize()).thenReturn(10);
        when(outboundBuffer.getWeight()).thenReturn(2000);
        when(workerManager.getRunningTasksCount()).thenReturn(5);

        HashMap<WorkerMonitorInfoEnum, Serializable> monitorInfo = new HashMap<>();
        monitor.captureMonitorInfo(monitorInfo);

        assertEquals(0, monitorInfo.get(WorkerMonitorInfoEnum.INBUFFER_SIZE_AVERAGE));
        assertEquals(0, monitorInfo.get(WorkerMonitorInfoEnum.OUTBUDDER_SIZE_AVERAGE));
        assertEquals(0, monitorInfo.get(WorkerMonitorInfoEnum.RUNNING_TASKS_AVERAGE));

        monitor.executeScheduled();

        monitor.captureMonitorInfo(monitorInfo);

        assertEquals(10, monitorInfo.get(WorkerMonitorInfoEnum.INBUFFER_SIZE_AVERAGE));
        assertEquals(2000, monitorInfo.get(WorkerMonitorInfoEnum.OUTBUDDER_SIZE_AVERAGE));
        assertEquals(5, monitorInfo.get(WorkerMonitorInfoEnum.RUNNING_TASKS_AVERAGE));

        monitor.captureMonitorInfo(monitorInfo);

        //after capture should be reset
        assertEquals(0, monitorInfo.get(WorkerMonitorInfoEnum.INBUFFER_SIZE_AVERAGE));
        assertEquals(0, monitorInfo.get(WorkerMonitorInfoEnum.OUTBUDDER_SIZE_AVERAGE));
        assertEquals(0, monitorInfo.get(WorkerMonitorInfoEnum.RUNNING_TASKS_AVERAGE));

        reset(workerManager, outboundBuffer);

        when(workerManager.getInBufferSize()).thenReturn(10);
        when(outboundBuffer.getWeight()).thenReturn(2000);
        when(workerManager.getRunningTasksCount()).thenReturn(5);

        monitor.executeScheduled();

        reset(workerManager, outboundBuffer);

        when(workerManager.getInBufferSize()).thenReturn(20);
        when(outboundBuffer.getWeight()).thenReturn(3000);
        when(workerManager.getRunningTasksCount()).thenReturn(7);

        monitor.executeScheduled();

        monitor.captureMonitorInfo(monitorInfo);

        assertEquals(15, monitorInfo.get(WorkerMonitorInfoEnum.INBUFFER_SIZE_AVERAGE));
        assertEquals(2500, monitorInfo.get(WorkerMonitorInfoEnum.OUTBUDDER_SIZE_AVERAGE));
        assertEquals(6, monitorInfo.get(WorkerMonitorInfoEnum.RUNNING_TASKS_AVERAGE));
    }

    @Configuration
    public static class MyTestConfig {
        @Bean public ScheduledWorkerLoadMonitor scheduledWorkerLoadMonitor() {return new ScheduledWorkerLoadMonitor();}
        @Bean public WorkerManager workerManager() {return mock(WorkerManager.class);}
        @Bean public EngineVersionService EngineVersionService() {return mock(EngineVersionService.class);}
        @Bean public OutboundBuffer outboundBuffer() {return mock(OutboundBuffer.class);}
        @Bean public WorkerNodeService workerNodeService() {return mock(WorkerNodeService.class);}
        @Bean public WorkerConfigurationService workerConfigurationService() {return mock(WorkerConfigurationService.class);}
        @Bean public WorkerRecoveryManager workerRecoveryManager() {return mock(WorkerRecoveryManager.class);}
        @Bean public WorkerVersionService workerVersionService() {return mock(WorkerVersionService.class);}
        @Bean public Integer numberOfExecutionThreads() {return 1;}
        @Bean public Long initStartUpSleep() {return 1L;}
        @Bean public Long maxStartUpSleep() {return 2L;}
        @Bean public String workerUuid() {return "1";}
    }
}
