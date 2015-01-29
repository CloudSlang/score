/*******************************************************************************
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *******************************************************************************/

package org.openscore.worker.management.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.openscore.events.EventBus;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.openscore.worker.management.monitor.ScheduledWorkerMonitor;
import org.openscore.worker.management.monitor.WorkerMonitors;
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
    EventBus eventBus;

    @Autowired
    WorkerMonitors workerMonitors;

    @Test
    public void testWorkerExecutionMonitorServiceCollectMonitor() throws InterruptedException {
        HashMap<WorkerMonitorInfoEnum, Serializable> monitorInfo = new HashMap<>();
        when(workerMonitors.getMonitorInfo()).thenReturn(monitorInfo);

        ScoreEvent scoreEvent = new ScoreEvent(EventConstants.WORKER_EXECUTION_MONITOR, monitorInfo);

        workerExecutionMonitorService.collectMonitorInformation();
        verify(eventBus, times(1)).dispatch(refEq(scoreEvent));
    }

    @Configuration
    public static class MyTestConfig {
        @Bean
        public WorkerExecutionMonitorService workerExecutionMonitorService() {return new WorkerExecutionMonitorServiceImpl();}
        @Bean public EventBus eventBus() {return mock(EventBus.class);}
        @Bean public WorkerMonitors workerMonitors() {return mock(WorkerMonitors.class);}
        @Bean public ScheduledWorkerMonitor scheduledWorkerMonitor() {return mock(ScheduledWorkerMonitor.class);}
    }
}
