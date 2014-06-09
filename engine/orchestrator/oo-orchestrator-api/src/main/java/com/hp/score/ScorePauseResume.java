package com.hp.score;

import java.io.Serializable;
import java.util.Map;

/**
 * User: maromg
 * Date: 29/05/2014
 */
interface ScorePauseResume {

    /**
     * Requests Score to pause the given execution. Only executions in status RUNNING can be paused.
     * @return true if the request was completed successfully or false if the execution does not exist or
     * is not in status RUNNING
     */
    boolean pauseExecution(Long executionId);

    /**
     * Requests Score to resume the given execution
     */
    //todo document this method better
    void resumeExecution(Long executionId, Map<String, Serializable> context, Map<String, Serializable> systemContext);

}
