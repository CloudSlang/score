/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.api;

import java.io.Serializable;
import java.util.Map;

/**
 * User: wahnonm
 * Date: 20/01/14
 * Time: 17:09
 */
public interface Score {

    /**
     * Trigger a flow by score & return the execution ID
     *
     * @param triggeringProperties object holding all the properties needed for the trigger
     * @return the execution ID
     */
    public Long trigger(TriggeringProperties triggeringProperties);

    /**
     * Requests Score to pause the given execution. Only executions in status RUNNING can be paused.
     *
     * @param executionId the ID of the execution
     * @return true if the request was completed successfully or false if the execution does not exist or
     * is not in status RUNNING
     */
    public boolean pauseExecution(Long executionId);

    /**
     * Requests Score to resume the given execution
     *
     * @param executionId   - the execution to resume
     * @param context  - the execution context values to run with
     * @param runtimeValues- values to add to the runtime values
     */
    public void resumeExecution(Long executionId, Map<String, Serializable> context, Map<String, Serializable> runtimeValues);

    /**
     * Trigger execution cancellation - sets the given execution with status PENDING_CANCEL
     * @param executionId - the execution to cancel
     */
    public void cancelExecution(Long executionId);

}
