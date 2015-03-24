/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.job;

import io.cloudslang.engine.queue.services.cleaner.QueueCleanerService;
import io.cloudslang.engine.queue.services.recovery.ExecutionRecoveryService;
import io.cloudslang.engine.versioning.services.VersionService;
import io.cloudslang.orchestrator.services.SplitJoinService;
import io.cloudslang.engine.partitions.services.PartitionTemplate;
import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
    @Qualifier("OO_EXECUTION_STATES")
    private PartitionTemplate execStatesPartitionTemplate;

    private final Logger logger = Logger.getLogger(getClass());

    final private int QUEUE_BULK_SIZE = 500;

    private final Integer SPLIT_JOIN_BULK_SIZE = Integer.getInteger("splitjoin.job.bulk.size", 25);

    private final Integer SPLIT_JOIN_ITERATIONS = Integer.getInteger("splitjoin.job.iterations", 20);

    /**
     * Job that will handle the cleaning of queue table.
     */
    @Override
    public void cleanQueueJob(){
        try {
            Set<Long> ids = queueCleanerService.getFinishedExecStateIds();
            if(logger.isDebugEnabled()) logger.debug("Will clean from queue the next Exec state ids amount:"+ids.size());

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
     * Job that will handle the joining of finished branches.
     */
    @Override
    public void joinFinishedSplitsJob(){
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
     * Job that will handle the rolling of Execution states rolling tables.
     */
    @Override
    public void statesRollingJob(){
        execStatesPartitionTemplate.onRolling();
    }

    /**
     * Job that will increment the recovery version
     */
    @Override
    public void recoveryVersionJob(){
        logger.debug("increment MSG_RECOVERY_VERSION Version");

        versionService.incrementVersion(VersionService.MSG_RECOVERY_VERSION_COUNTER_NAME);
    }

    /**
     * Job to execute the recovery check.
     */
    @Override
    public void executionRecoveryJob(){
        if (logger.isDebugEnabled()) {
            logger.debug("ExecutionRecoveryJob woke up at " + new Date());
        }

        try {
            executionRecoveryService.doRecovery();
        }
        catch (Exception e){
            logger.error("Can't run queue recovery job.",e);
        }
    }

}
