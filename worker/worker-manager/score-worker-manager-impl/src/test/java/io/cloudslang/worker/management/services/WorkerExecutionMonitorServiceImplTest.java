/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package io.cloudslang.worker.management.services;

import io.cloudslang.score.events.ConfigurationAwareEventBus;
import org.junit.Test;
import org.junit.runner.RunWith;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.worker.management.monitor.ScheduledWorkerMonitor;
import io.cloudslang.worker.management.monitor.WorkerMonitors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.Serializable;
import java.util.HashMap;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WorkerExecutionMonitorServiceImplTest.MyTestConfig.class)
public class WorkerExecutionMonitorServiceImplTest {
    @Autowired
    WorkerExecutionMonitorService workerExecutionMonitorService;

    @Autowired
    ConfigurationAwareEventBus eventBus;

    @Autowired
    WorkerMonitors workerMonitors;

    @Test
    public void testWorkerExecutionMonitorServiceCollectMonitor() throws InterruptedException {
        HashMap<WorkerMonitorInfoEnum, Serializable> monitorInfo = new HashMap<>();
        when(workerMonitors.getMonitorInfo()).thenReturn(monitorInfo);

        ScoreEvent scoreEvent = new ScoreEvent(EventConstants.WORKER_EXECUTION_MONITOR, monitorInfo);

        workerExecutionMonitorService.collectMonitorInformation();
        verify(eventBus).dispatchEvent(refEq(scoreEvent));
    }

    @Configuration
    public static class MyTestConfig {
        @Bean
        public WorkerExecutionMonitorService workerExecutionMonitorService() {return new WorkerExecutionMonitorServiceImpl();}
        @Bean public ConfigurationAwareEventBus eventBus() {return mock(ConfigurationAwareEventBus.class);}
        @Bean public WorkerMonitors workerMonitors() {return mock(WorkerMonitors.class);}
        @Bean public ScheduledWorkerMonitor scheduledWorkerMonitor() {return mock(ScheduledWorkerMonitor.class);}
    }
}
