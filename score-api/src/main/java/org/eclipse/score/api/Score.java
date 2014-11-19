/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.api;

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
