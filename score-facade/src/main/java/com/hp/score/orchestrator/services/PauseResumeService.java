package com.hp.score.orchestrator.services;

import com.hp.score.facade.execution.ExecutionSummary;
import com.hp.score.facade.execution.PauseReason;
import com.hp.score.facade.entities.Execution;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kravtsov
 * Date: 17/12/12
 * Time: 15:34
 */
public interface PauseResumeService {
    /**
     * Pauses execution with type PENDING_PAUSE
     *
     * @param executionId of the execution
     */
    Long pauseExecution(Long executionId, String branchId, PauseReason reason);

    /**
     * Resumes execution and puts it back to execution queue
     *
     * @param executionId of the execution
     */
    void resumeExecution(Long executionId, String branchId, Map<String, Serializable> map);

    /**
     * Persists Execution object to the DB
     *
     * @param executionId - execution id
     * @param branchId    - branch id if it is parallel lane
     * @param execution   - object to persist
     * @return boolean -  true if write was successful
     */
    PauseReason writeExecutionObject(Long executionId, String branchId, Execution execution);

    /**
     * Returns list of strings: each one of form: executionId:branchId
     *
     * @return list of execution
     */
    Set<String> readAllPausedExecutionBranchIds();

    /**
     * Returns the execution if its status is Paused*. Otherwise returns null.
     * TODO:
     *  After moving PauseResumeService to the engine, this method should returns ExecutionSummaryEntity,
     *  and the EngineFacadeImpl should map it to ExecutionSummary.
     *  In general, this module shouldn't use classes from the facade
     */
    ExecutionSummary readPausedExecution(Long executionId, String branchId);

    /**
     * Get a list of all pause id's that are relevant to an execution,
     * there could be many because of different lanes that can be paused
     *
     * @param executionId th execution id in question
     * @return a list of all current pauses id relevant
     */
    List<Long> readPauseIds(Long executionId);
}