package com.hp.score.jobs;

/**
 * User: wahnonm
 * Date: 13/08/14
 * Time: 10:35
 */
public interface ScoreEngineJobs {

    void cleanQueueJob();

    void joinFinishedSplitsJob();

    void statesRollingJob();

    void recoveryVersionJob();

    void executionRecoveryJob();

}
