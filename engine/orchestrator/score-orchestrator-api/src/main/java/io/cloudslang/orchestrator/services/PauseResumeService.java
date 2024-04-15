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

import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.facade.execution.ExecutionSummary;
import io.cloudslang.score.facade.execution.PauseReason;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PauseResumeService {

    /**
     * Pauses execution with type PENDING_PAUSE
     *
     * @param executionId id of the execution
     * @param branchId id of the branch of the execution we want to pause
     * @param reason the pause reason
     * @return paused execution id but in case the execution is already paused then return null
     */
    Long pauseExecution(Long executionId, String branchId, PauseReason reason);

    /**
     * Add interrupts to the system context under USER_INTERRUPT
     *
     * @param executionId id of the execution
     * @return add interrupts to the current execution
     */
    void injectInterrupts(Long executionId, Map<String, Set<String>> interrupts);

    /**
     * removes interrupts
     *
     * @param executionId id of the execution
     * @return add interrupts to the current execution
     */
    void deleteInterrupts(Long executionId, Map<String, Set<String>> interrupts);

    /**
     * Resumes execution and puts it back to execution queue
     *
     * @param executionId id of the paused execution we want to resume
     * @param branchId id of the branch of the execution we want to resume
     * @param map the values to run with
     */
    void resumeExecution(Long executionId, String branchId, Map<String, Serializable> map);

    /**
     * Persists Execution object to the DB
     *
     * @param executionId - execution id
     * @param branchId - branch id if it is parallel lane
     * @param execution - object to persist
     * @return the pause reason of the paused execution
     */
    PauseReason writeExecutionObject(Long executionId, String branchId, Execution execution, boolean updateParentExecObject);

    /**
     * Uses caching Returns list of strings: each one of form: executionId:branchId
     *
     * @return list of execution & branch ids of all the paused branches
     */
    Set<String> readAllPausedExecutionBranchIds();

    /**
     * Does not use caching Returns list of strings: each one of form: executionId:branchId
     *
     * @return list of execution & branch ids of all the paused branches
     */
    Set<String> readAllPausedExecutionBranchIdsNoCache();

    /**
     * Returns the execution if its status is Paused*. Otherwise returns null.
     *
     * @param executionId id of the execution
     * @param branchId id of the branch
     * @return the execution if its status is Paused*. Otherwise returns null.
     */
    ExecutionSummary readPausedExecution(Long executionId, String branchId);

    /**
     * Get a list of all pause id's that are relevant to an execution, there could be many because of different lanes
     * that can be paused
     *
     * @param executionId th execution id in question
     * @return a list of all current pauses id relevant
     */
    List<Long> readPauseIds(Long executionId);

    void createNoRobotGroup(Execution execution, Long pauseId, String branchId);

    void deletePauseData(String executionId, String branchId);
}