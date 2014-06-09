package com.hp.score.services;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
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
    public ExecutionState readByExecutionIdAndBranchId(String executionId, String branchId);

    /**
     * Reads all the runs with the specified run id
     */
    public List<ExecutionState> readByExecutionId(String executionId);

    public List<String> readExecutionIdByStatuses(List<ExecutionEnums.ExecutionStatus> statuses);

    /**
     * Reads a cancelled parent run with the specified run id
     */
    public ExecutionState readCancelledExecution(String executionId);

    /**
     * Creates a new parent run with the specified run id. The run is created as a parent run.
     */
    public ExecutionState createParentExecution(String executionId);

    /**
     * Creates a new run state object
     */
    public ExecutionState createExecutionState(String executionId, String branchId);

    /**
     * Returns the run object for the specified run id and branch id
     */
    public Execution readExecutionObject(String executionId, String branchId);

    /**
     * Updates the run object for the specified run id and branch id
     */
    public void updateExecutionObject(String executionId, String branchId, Execution execution);

    public void updateExecutionStateStatus(String executionId, String branchId, ExecutionEnums.ExecutionStatus status);

    /**
     * Deletes the specified run, both the parent run and any child runs
     */
    public void deleteExecutionState(String executionId, String branchId);
}
