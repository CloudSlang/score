package com.hp.score.job;

/**
 * User: wahnonm
 * Date: 13/08/14
 * Time: 10:35
 */
//TODO: Add Javadoc
//TODO: Do we want to move to api module?
public interface ScoreEngineJobs {

    void cleanQueueJob();

    void joinFinishedSplitsJob();

    void statesRollingJob();

    void recoveryVersionJob();

    void executionRecoveryJob();

}
