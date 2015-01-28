package org.openscore.worker.management.services;

import org.apache.log4j.Logger;
import org.openscore.events.EventBus;
import org.openscore.events.EventConstants;
import org.openscore.events.ScoreEvent;
import org.openscore.worker.management.monitor.ScheduledWorkerMonitor;
import org.openscore.worker.management.monitor.WorkerMonitors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

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
        logger.info("Collecting Worker Monitors");
        try {
            HashMap<WorkerMonitorInfoEnum, Serializable> monitorInfo = workerMonitors.getMonitorInfo();
            logger.info("Sending Worker Monitors Info to central:[" + monitorInfo +"]");
            ScoreEvent event = new ScoreEvent(EventConstants.WORKER_EXECUTION_MONITOR, monitorInfo);
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
