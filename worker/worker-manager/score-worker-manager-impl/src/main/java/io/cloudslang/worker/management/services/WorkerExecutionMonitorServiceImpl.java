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

import org.apache.log4j.Logger;
import io.cloudslang.events.EventBus;
import io.cloudslang.events.EventConstants;
import io.cloudslang.events.ScoreEvent;
import io.cloudslang.worker.management.monitor.ScheduledWorkerMonitor;
import io.cloudslang.worker.management.monitor.WorkerMonitors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service
public class WorkerExecutionMonitorServiceImpl implements WorkerExecutionMonitorService{
    protected static final Logger logger = Logger.getLogger(WorkerExecutionMonitorServiceImpl.class);

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
