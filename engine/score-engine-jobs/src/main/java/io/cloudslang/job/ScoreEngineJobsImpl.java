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
import io.cloudslang.orchestrator.services.ExecutionCleanerService;
import io.cloudslang.orchestrator.services.SplitJoinService;
import io.cloudslang.orchestrator.services.SuspendedExecutionCleanerService;
import org.apache.commons.lang.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

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

    @Autowired
    private ExecutionCleanerService executionCleanerService;

    private final Logger logger = LogManager.getLogger(getClass());

    final private int QUEUE_BULK_SIZE = 500;

    private final Integer SPLIT_JOIN_BULK_SIZE = Integer.getInteger("splitjoin.job.bulk.size", 25);

    private final Integer SPLIT_JOIN_ITERATIONS = Integer.getInteger("splitjoin.job.iterations", 20);

    private final Integer CLEAN_SUSPENDED_EXECUTIONS_BULK_SIZE = Integer.getInteger("cleanSuspendedExecutions.job.bulk.size", 200);

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
            if (logger.isDebugEnabled()) {
                logger.debug("MiMergeBranchesContextsJob woke up at " + new Date());
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            // try sequentially at most 'ITERATIONS' attempts
            // quit when there aren't any more results to process
            boolean moreToJoin;

            for (int i = 0; i < SPLIT_JOIN_ITERATIONS; i++) {
                int joinedSplits = splitJoinService.joinFinishedMiBranches(SPLIT_JOIN_BULK_SIZE);
                moreToJoin = (joinedSplits == SPLIT_JOIN_BULK_SIZE);
                if (!moreToJoin) {
                    break;
                }
            }

            stopWatch.stop();
            if (logger.isDebugEnabled()) logger.debug("finished MiContextsMediatorJob in " + stopWatch);
        } catch (Exception ex) {
            logger.error("MiContextsMediatorJob failed", ex);
        }
    }

    @Override
    public void cleanSuspendedExecutions() {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("CleanSuspendedExecutions woke up at " + new Date());
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            splitJoinService.deleteFinishedSuspendedExecutions(CLEAN_SUSPENDED_EXECUTIONS_BULK_SIZE);

            stopWatch.stop();
            if (logger.isDebugEnabled()) {
                logger.debug("finished CleanSuspendedExecutions in " + stopWatch);
            }
        } catch (Exception ex) {
            logger.error("CleanSuspendedExecutions failed", ex);
        }
    }

    @Override
    public void cleanFinishedExecutionState() {
        if (logger.isDebugEnabled()) {
            logger.debug("started in CleanFinishedExecutionState method");
        }

        try {
            executionCleanerService.cleanExecutions();
        } catch (Exception e) {
            logger.error("Can't run finished execution state cleaner job. : " + e);
        }
    }
}
