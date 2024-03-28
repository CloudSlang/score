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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.cloudslang.score.events.EventBus;
import io.cloudslang.score.events.EventConstants;
import io.cloudslang.score.events.ScoreEvent;
import io.cloudslang.worker.management.monitor.ScheduledWorkerMonitor;
import io.cloudslang.worker.management.monitor.WorkerMonitors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service
public class WorkerExecutionMonitorServiceImpl implements WorkerExecutionMonitorService{
    protected static final Logger logger = LogManager.getLogger(WorkerExecutionMonitorServiceImpl.class);

    @Autowired
    private EventBus eventBus;

    @Autowired
    private WorkerMonitors workerMonitors;

    @Autowired
    private List<ScheduledWorkerMonitor> scheduledWorkerMonitors;

    @Override
    public void collectMonitorInformation() {
        if(logger.isDebugEnabled()) {
            logger.debug("Collecting Worker Monitors");
        }
        try {
            Map<WorkerMonitorInfoEnum, Serializable> monitorInfo = workerMonitors.getMonitorInfo();
            if(logger.isDebugEnabled()) {
                logger.debug("Sending Worker Monitors Info:[" + monitorInfo +"]");
            }
            ScoreEvent event = new ScoreEvent(EventConstants.WORKER_EXECUTION_MONITOR, (Serializable) monitorInfo);
            eventBus.dispatch(event);
        } catch (InterruptedException e) {
            logger.error("Failed to dispatch monitor info event", e);
        }
    }

    @Override
    public void executeScheduledWorkerMonitors() {
        for (ScheduledWorkerMonitor scheduledWorkerMonitor : scheduledWorkerMonitors) {
            scheduledWorkerMonitor.executeScheduled();
        }
    }
}
