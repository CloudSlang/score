package com.hp.oo.orchestrator.services;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;


public class SplitJoinJob { //implements StatefulJob {
    private final Logger logger = Logger.getLogger(getClass());

    private final Integer BULK_SIZE = Integer.getInteger("splitjoin.job.bulk.size", 25);
    private final Integer ITERATIONS = Integer.getInteger("splitjoin.job.iterations", 20);

    @Autowired
    private SplitJoinService splitJoinService;

//    @Override
//    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
//        try {
//            if (logger.isDebugEnabled()) logger.debug("SplitJoinJob woke up at " + new Date());
//            StopWatch stopWatch = new StopWatch();
//            stopWatch.start();
//
//            // try sequentially at most 'ITERATIONS' attempts
//            // quit when there aren't any more results to process
//            boolean moreToJoin = true;
//            for (int i = 0; i < ITERATIONS && moreToJoin; i++) {
//                int joinedSplits = splitJoinService.joinFinishedSplits(BULK_SIZE);
//                moreToJoin = (joinedSplits == BULK_SIZE);
//            }
//
//            stopWatch.stop();
//            if (logger.isDebugEnabled()) logger.debug("finished SplitJoinJob in " + stopWatch);
//        } catch (Exception ex) {
//            logger.error("SplitJoinJob failed", ex);
//        }
//    }
}