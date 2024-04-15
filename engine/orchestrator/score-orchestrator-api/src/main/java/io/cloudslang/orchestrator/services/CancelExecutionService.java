/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cloudslang.orchestrator.services;

import io.cloudslang.score.facade.execution.ExecutionActionResult;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hajyhia
 * Date: 3/29/13
 * Time: 10:48 AM
 */
public interface CancelExecutionService {

    /**
     * Trigger execution cancellation - sets the given execution with status PENDING_CANCEL
     * @param executionId - the execution to cancel
     *
     */
    ExecutionActionResult requestCancelExecution(Long executionId);

    /**
     * Returns list of executions that are were cancelled (the status is either CANCELED or PENDING_CANCEL)
     * We need it for the cache of cancelled executions in the worker.
     */
    List<Long> readCanceledExecutionsIds();

    /**
     * Returns true if the given execution was cancelled.
     * @param executionId - the execution to check.
     * @return true if the given execution's status is CANCELED or PENDING_CANCEL
     */
    boolean isCanceledExecution(Long executionId);
}
