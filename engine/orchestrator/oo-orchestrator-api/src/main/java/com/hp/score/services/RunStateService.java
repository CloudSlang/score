package com.hp.score.services;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.score.entities.RunState;

import java.util.List;

/**
 * User: maromg
 * Date: 12/05/2014
 */
public interface RunStateService {

    /**
     * Reads the run with the specified run id and branch id
     */
    public RunState readByRunIdAndBranchId(String runId, String branchId);

    /**
     * Reads all the runs with the specified run id
     */
    public List<RunState> readByRunId(String runId);

    public List<String> readRunIdAndBranchIdByStatuses(List<ExecutionEnums.ExecutionStatus> statuses);

    /**
     * Reads a cancelled parent run with the specified run id
     */
    public RunState readCancelledRun(String runId);

    /**
     * Creates a new parent run with the specified run id. The run is created as a parent run.
     */
    public void createParentRun(String runId);

    /**
     * Creates a new run state object
     */
    public void createRunState(String runId, String branchId);

    /**
     * Returns the run object for the specified run id and branch id
     */
    public Execution readRunObject(String runId, String branchId);

    /**
     * Updates the run object for the specified run id and branch id
     */
    public void updateRunObject(String runId, String branchId, Execution execution);

    public void updateRunStateStatus(String runId, String branchId, ExecutionEnums.ExecutionStatus status);

    /**
     * Deletes the specified run, both the parent run and any child runs
     */
    public void deleteRunState(String runId, String branchId);
}
