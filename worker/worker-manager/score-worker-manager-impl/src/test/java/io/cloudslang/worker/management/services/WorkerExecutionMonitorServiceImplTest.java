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

package io.cloudslang.worker.management.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import io.cloudslang.score.events.EventBus;
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
