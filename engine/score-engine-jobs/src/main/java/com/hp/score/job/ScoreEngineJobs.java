package com.hp.score.job;

/**
 * User: wahnonm
 * Date: 13/08/14
 * Time: 10:35
 */
//TODO: Add Javadoc
public interface ScoreEngineJobs {

    /**
     * job that clean the finished steps from the queue
     */
    void cleanQueueJob();

    /**
     * job that join all the suspended execution of brunches that finished
     */
    void joinFinishedSplitsJob();

    /**
     * job that create rolling in the partition table
     */
    void statesRollingJob();

    /**
     * job that update version number - we use it instead of time
     */
    void recoveryVersionJob();

    /**
     *  job that recover workers that didn't send keep alive
     */
    void executionRecoveryJob();

}
