package com.hp.score.orchestrator.services;

import java.io.Serializable;
import java.util.Map;

/**
 * User: maromg
 * Date: 29/05/2014
 */
interface ScorePauseResume {

    /**
     * Requests Score to pause the given execution. Only executions in status RUNNING can be paused.
     *
     * @return true if the request was completed successfully or false if the execution does not exist or
     * is not in status RUNNING
     */
    boolean pauseExecution(Long executionId);

    /**
     * Requests Score to resume the given execution
     * @param executionId   - the execution to resume
     * @param context  - the execution context values to run with
     * @param runtimeValues- values to add to the runtime values
     */
    void resumeExecution(Long executionId, Map<String, Serializable> context, Map<String, Serializable> runtimeValues);

}
