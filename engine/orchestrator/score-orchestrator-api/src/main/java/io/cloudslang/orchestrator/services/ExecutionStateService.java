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
import io.cloudslang.score.facade.execution.ExecutionStatus;
import io.cloudslang.orchestrator.entities.ExecutionState;
import org.springframework.data.domain.PageRequest;

import java.util.Date;
import java.util.List;

/**
 * User:
 * Date: 12/05/2014
 */
public interface ExecutionStateService {

    /**
     * Reads the run with the specified execution id and branch id
     *
     * @param executionId id of the execution
     * @param branchId    id of the branch
     * @return the execution state
     */
    public ExecutionState readByExecutionIdAndBranchId(Long executionId, String branchId);

    /**
     * Reads all the runs with the specified execution id
     *
     * @param executionId id of the execution
     * @return all the execution states with the given execution id
     */
    public List<ExecutionState> readByExecutionId(Long executionId);

    /**
     * Find all the executions with the given statuses
     *
     * @param statuses list of execution statuses
     * @return execution ids with any of teh given statuses
     */
    public List<Long> readExecutionIdByStatuses(List<ExecutionStatus> statuses);

    /**
     * Reads a cancelled parent execution with the specified execution id
     *
     * @param executionId id of the execution
     * @return the canceled execution state with the given executionId
     */
    public ExecutionState readCancelledExecution(Long executionId);

    /**
     * Creates a new parent execution with the specified execution id. The execution is created as a parent execution.
     *
     * @param executionId id of the execution
     * @return the new execution state object
     */
    public ExecutionState createParentExecution(Long executionId);

    /**
     * Creates a new execution state object
     *
     * @param executionId id of the execution
     * @param branchId    id of the branch
     * @return the execution state
     */
    public ExecutionState createExecutionState(Long executionId, String branchId);

    /**
     * Returns the execution object for the specified execution id and branch id
     *
     * @param executionId id of the execution
     * @param branchId    id of the branch
     * @return the execution object
     */
    public Execution readExecutionObject(Long executionId, String branchId);

    /**
     * Updates the execution object for the specified execution id and branch id
     *
     * @param executionId id of the execution
     * @param branchId    id of the branch
     * @param execution   the execution object
     */
    public void updateExecutionObject(Long executionId, String branchId, Execution execution, Date updateDate);

    /***
     * Updates the status for the given execution id and branch id
     *
     * @param executionId id of the execution
     * @param branchId id of the branch
     * @param status  status of the execution
     * @param updateDate update time of the execution
     */
    public void updateExecutionStateStatus(Long executionId, String branchId, ExecutionStatus status, Date updateDate);

    /**
     * Deletes the specified execution, both the parent execution and any child executions
     *
     * @param executionId id of the execution
     * @param branchId    id of the branch
     */
    public void deleteExecutionState(Long executionId, String branchId);

    public void deleteCanceledExecutionStates();

    public Execution getExecutionObjectForNullBranch(Long executionId);

    List<Long> findExecutionStateByStatusInAndUpdateTimeLessThanEqual(List<ExecutionStatus> statuses, Long time, PageRequest pageRequest);

    void deleteExecutionStateByIds(List<Long> ids);
}
