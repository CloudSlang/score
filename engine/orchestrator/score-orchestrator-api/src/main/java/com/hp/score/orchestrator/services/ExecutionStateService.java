package com.hp.score.orchestrator.services;

import com.hp.score.facade.entities.Execution;
import com.hp.score.facade.execution.ExecutionStatus;
import com.hp.score.orchestrator.entities.ExecutionState;

import java.util.List;

/**
 * User: maromg
 * Date: 12/05/2014
 */
public interface ExecutionStateService {

    /**
     * Reads the run with the specified execution id and branch id
     *
     * @param executionId id of the execution
     * @param branchId id of the branch
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
     * @param branchId id of the branch
     * @return the execution state
     */
    public ExecutionState createExecutionState(Long executionId, String branchId);

    /**
     * Returns the execution object for the specified execution id and branch id
     *
     * @param executionId id of the execution
     * @param branchId id of the branch
     * @return the execution object
     */
    public Execution readExecutionObject(Long executionId, String branchId);

    /**
     * Updates the execution object for the specified execution id and branch id
     *
     * @param executionId id of the execution
     * @param branchId id of the branch
     * @param execution the execution object
     */
    public void updateExecutionObject(Long executionId, String branchId, Execution execution);

    /***
     * Updates the status for the given execution id and branch id
     *
     * @param executionId id of the execution
     * @param branchId id of the branch
     * @param status  status of the execution
     */
    public void updateExecutionStateStatus(Long executionId, String branchId, ExecutionStatus status);

    /**
     * Deletes the specified execution, both the parent execution and any child executions
     *
     * @param executionId id of the execution
     * @param branchId id of the branch
     */
    public void deleteExecutionState(Long executionId, String branchId);
}
