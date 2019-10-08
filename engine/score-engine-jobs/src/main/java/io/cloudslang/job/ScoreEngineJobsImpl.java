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

package io.cloudslang.job;

import io.cloudslang.engine.queue.services.LargeMessagesMonitorService;
import io.cloudslang.engine.queue.services.cleaner.QueueCleanerService;
import io.cloudslang.engine.queue.services.recovery.ExecutionRecoveryService;
import io.cloudslang.engine.versioning.services.VersionService;
import io.cloudslang.orchestrator.services.SplitJoinService;
import io.cloudslang.orchestrator.services.SuspendedExecutionCleanerService;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This class will unite all periodic jobs needed by the score engine, to be triggered by a scheduler .
 * User: wahnonm
 * Date: 12/08/14
 * Time: 16:18
 */
public class ScoreEngineJobsImpl implements ScoreEngineJobs {

    @Autowired
    private QueueCleanerService queueCleanerService;

    @Autowired
    private SplitJoinService splitJoinService;

    @Autowired
    private VersionService versionService;

    @Autowired
    private ExecutionRecoveryService executionRecoveryService;

    @Autowired
    private SuspendedExecutionCleanerService suspendedExecutionCleanerService;

    @Autowired
    private LargeMessagesMonitorService largeMessagesMonitorService;

    private final Logger logger = Logger.getLogger(getClass());

    final private int QUEUE_BULK_SIZE = 500;

    private final Integer SPLIT_JOIN_BULK_SIZE = Integer.getInteger("splitjoin.job.bulk.size", 25);

    private final Integer SPLIT_JOIN_ITERATIONS = Integer.getInteger("splitjoin.job.iterations", 20);

    /**
     * Job that will handle the cleaning of queue table.
     */
    @Override
    public void cleanQueueJob() {
        try {
            Set<Long> ids = queueCleanerService.getFinishedExecStateIds();
            if (logger.isDebugEnabled())
                logger.debug("Will clean from queue the next Exec state ids amount:" + ids.size());

            Set<Long> execIds = new HashSet<>();

            for (Long id : ids) {
                execIds.add(id);
                if (execIds.size() >= QUEUE_BULK_SIZE) {
                    queueCleanerService.cleanFinishedSteps(execIds);
                    execIds.clear();
                }
            }

            if (execIds.size() > 0) {
                queueCleanerService.cleanFinishedSteps(execIds);
            }
        } catch (Exception e) {
            logger.error("Can't run queue cleaner job.", e);
        }
    }

    /**
     * Job that will handle the joining of finished branches for parallel and non-blocking steps.
     */
    @Override
    public void joinFinishedSplitsJob() {
        try {
            if (logger.isDebugEnabled()) logger.debug("SplitJoinJob woke up at " + new Date());
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // try sequentially at most 'ITERATIONS' attempts
            // quit when there aren't any more results to process
            boolean moreToJoin = true;
            for (int i = 0; i < SPLIT_JOIN_ITERATIONS && moreToJoin; i++) {
                int joinedSplits = splitJoinService.joinFinishedSplits(SPLIT_JOIN_BULK_SIZE);
                moreToJoin = (joinedSplits == SPLIT_JOIN_BULK_SIZE);
            }

            stopWatch.stop();
            if (logger.isDebugEnabled()) logger.debug("finished SplitJoinJob in " + stopWatch);
        } catch (Exception ex) {
            logger.error("SplitJoinJob failed", ex);
        }
    }

    /**
     * Job that will increment the recovery version
     */
    @Override
    public void recoveryVersionJob() {
        logger.debug("increment MSG_RECOVERY_VERSION Version");

        versionService.incrementVersion(VersionService.MSG_RECOVERY_VERSION_COUNTER_NAME);
    }

    /**
     * Job to execute the recovery check.
     */
    @Override
    public void executionRecoveryJob() {
        if (logger.isDebugEnabled()) {
            logger.debug("ExecutionRecoveryJob woke up at " + new Date());
        }

        try {
            executionRecoveryService.doRecovery();
        } catch (Exception e) {
            logger.error("Can't run queue recovery job.", e);
        }
    }

    /**
     * clean suspended executions
     */
    @Override
    public void cleanSuspendedExecutionsJob() {

        if (logger.isDebugEnabled()) {
            logger.debug("CleanSuspendedExecutionJob woke up at " + new Date());
        }

        try {
            suspendedExecutionCleanerService.cleanupSuspendedExecutions();
        } catch (Exception e) {
            logger.error("Can't run suspended execution cleaner job.", e);
        }
    }

    @Override
    public void monitorLargeMessagesJob() {

        if (logger.isDebugEnabled()) {
            logger.debug("MonitorLargeMessagesJob woke up!");
        }

        largeMessagesMonitorService.monitor();
    }

    @Override
    public void miMergeBranchesContexts() {
        try {
            if (logger.isDebugEnabled()) logger.debug("MiMergeBranchesContextsJob woke up at " + new Date());
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // try sequentially at most 'ITERATIONS' attempts
            // quit when there aren't any more results to process
            boolean moreToJoin = true;
            for (int i = 0; i < SPLIT_JOIN_ITERATIONS && moreToJoin; i++) {
                int joinedSplits = splitJoinService.joinFinishedMiBranches(SPLIT_JOIN_BULK_SIZE);
                moreToJoin = (joinedSplits == SPLIT_JOIN_BULK_SIZE);
            }

            stopWatch.stop();
            if (logger.isDebugEnabled()) logger.debug("finished SplitJoinJob in " + stopWatch);
        } catch (Exception ex) {
            logger.error("SplitJoinJob failed", ex);
        }
    }
}
