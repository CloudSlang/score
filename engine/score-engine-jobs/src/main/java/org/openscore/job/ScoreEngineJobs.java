/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.openscore.job;

/**
 * User: wahnonm
 * Date: 13/08/14
 * Time: 10:35
 */
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
