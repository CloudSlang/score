package com.hp.score.services;

import com.hp.oo.enginefacade.execution.ExecutionStatus;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.score.entities.ExecutionState;

import java.util.List;

/**
 * User: maromg
 * Date: 12/05/2014
 */
public interface ExecutionStateService {

    /**
     * Reads the run with the specified run id and branch id
     */
    public ExecutionState readByExecutionIdAndBranchId(Long executionId, String branchId);

    /**
     * Reads all the runs with the specified run id
     */
    public List<ExecutionState> readByExecutionId(Long executionId);

    public List<Long> readExecutionIdByStatuses(List<ExecutionStatus> statuses);

    /**
     * Reads a cancelled parent run with the specified run id
     */
    public ExecutionState readCancelledExecution(Long executionId);

    /**
     * Creates a new parent run with the specified run id. The run is created as a parent run.
     */
    public ExecutionState createParentExecution(Long executionId);

    /**
     * Creates a new run state object
     */
    public ExecutionState createExecutionState(Long executionId, String branchId);

    /**
     * Returns the run object for the specified run id and branch id
     */
    public Execution readExecutionObject(Long executionId, String branchId);

    /**
     * Updates the run object for the specified run id and branch id
     */
    public void updateExecutionObject(Long executionId, String branchId, Execution execution);

    public void updateExecutionStateStatus(Long executionId, String branchId, ExecutionStatus status);

    /**
     * Deletes the specified run, both the parent run and any child runs
     */
    public void deleteExecutionState(Long executionId, String branchId);
}
