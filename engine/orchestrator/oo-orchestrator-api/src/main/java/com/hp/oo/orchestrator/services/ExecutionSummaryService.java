package com.hp.oo.orchestrator.services;

//import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.orchestrator.entities.ExecutionSummaryEntity;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: butensky
 * Date: 20/02/13
 * Time: 15:26
 */
public interface ExecutionSummaryService {

    /**
     * Returns list of Executions that started on the given startDate, or afterwards.
     *
     * @param pageNum   - the page to return, one-based.
     * @param pageSize  - the number of executions to return  @return a list of Executions that started on the given startDate or before.
     */
    List<ExecutionSummaryEntity> readExecutions(String flowPath,
                                                List<ExecutionEnums.ExecutionStatus> statuses,
                                                List<String> resultStatusTypes,
                                                List<PauseReason> pauseReasons,
                                                String owner,
                                                Date startedBefore,
                                                Date startedAfter,
                                                int pageNum,
                                                int pageSize);

    /**
     * Returns list of ExecutionSummary objects, filtered by the given executionIds and statuses.
     *
     * @param executionIds - the ids of the Executions to return.
     * @param statuses     - filter by the given statuses.
     * @return ExecutionSummaryEntity if its executionId is in the given executionIds list, and its status is in the given statuses list.
     */
    List<ExecutionSummaryEntity> readExecutionsByIdsAndStatus(List<String> executionIds, List<ExecutionEnums.ExecutionStatus> statuses);

    /**
     * Returns list of ExecutionSummaryEntity for the given executions Ids.
     *
     * @param executionIds the executions to return
     * @return a list of ExecutionSummaryEntity objects, corresponding to the given executionIds.
     */
    List<ExecutionSummaryEntity> readExecutionsByIds(List<String> executionIds);

    /**
     * Returns data concerning a specific executionId in the system.
     *
     * @param executionId the execution id
     * @return an ExecutionSummaryEntity object, or null if the given Execution wasn't found.
     */
    ExecutionSummaryEntity readExecutionSummary(String executionId); // used by FlowExecutionController in flow-execution-impl

    /**
     * Creates a new Execution in the DB.
     *
     * @param executionId   - mandatory
     * @param branchId      - nullable. relevant for parallel runs.
     * @param startTime     - mandatory.
     * @param initialStatus - mandatory.
     * @param executionName - nullable?
     * @param flowUuid      - mandatory.
     * @param flowPath      - mandatory.
     * @param triggeredBy   - mandatory. The value will be set as the Owner of the execution as well.     @return the created ExecutionSummary.
     * @param triggeringSource -nullable
     */
    ExecutionSummaryEntity createExecution(String executionId, String branchId, Date startTime, ExecutionEnums.ExecutionStatus initialStatus, String executionName, String flowUuid, String flowPath, String triggeredBy, String triggeringSource);

    /**
     * Updates existing execution with current status. For instance: SYSTEM_FAILURE \ SYSTEM_CANCEL \ USER_CANCEL.
     *
     *
     * @param executionId     - the execution Id.
     * @param executionStatus - CANCELED \ SYSTEM_FAILURE.
     * @param endTime         - the time that the run ended.
     * @param roi - the ROI of the run
     * @return the Updated ExecutionSummary object, or null if the given Execution wasn't found.
     * @Note For COMPLETED status, use designated method!
     * @Note For Paused statuses, use methods in PauseResumeService.
     */
    ExecutionSummaryEntity updateExecutionStatus(String executionId, ExecutionEnums.ExecutionStatus executionStatus, Date endTime, Double roi);

    /**
     * Update the execution as Completed.
     *
     *
     * @param executionId      - the execution Id.
     * @param resultStatusType - the type of the flow's last step. For instance (AFLs values) - RESOLVED, ERROR, NO ACTION TAKEN, DIAGNOSED.
     * @param resultStatusName - the specific name of the flow's last step. For instance - success.
     * @param endTime          - the time that the run completed.
     * @param roi - the final ROI of the run.
     * @return the Updated ExecutionSummary object, or null if the given Execution wasn't found.
     */
    ExecutionSummaryEntity updateExecutionCompletion(String executionId, String resultStatusType, String resultStatusName, Date endTime, Double roi);

    /**
     * Updates the current owner of the execution
     * @param executionId       - the execution id
     * @param owner             - the owner to update to
     * @return the updated ExecutionSummary object, or null if the given Execution wasn't found.
     */
    ExecutionSummaryEntity updateExecutionOwner(String executionId, String owner);

    /**
     * Calculates the result distribution of a specific flow(how many succeeded,failed...)
     *
     * @param flowUuid - the flow uuid to search by.
     * @return a map where the key is the result name, and the value is the amount of executions that finished with this
     *         result(for the given flow uuid).
     */
    Map<String, Long> readResultDistributionOfFlow(String flowUuid);


    /**
     * Returns list of Executions for a specific flow that started after startDate and before endTime,
     * starting from offsetIndex, with size pageSize, and ordered in ascending order.
     *
     * @param flowUuid    : the flow uuid to search by.
     * @param startTime   : execution started after this given time.
     * @param endTime     : execution ended before this given time.
     * @param offsetIndex : the offset index of the result, say between startTime and endTime we have 100 executions,
     *                    then if we set this param to 50 (assuming pageSize bigger then 50),
     *                    we will get 50 executions in the result.
     * @param pageSize    : the amount of max result to return.
     */
    public List<ExecutionSummaryEntity> readExecutionsByFlow(String flowUuid, Date startTime, Date endTime, int offsetIndex, int pageSize);


    /**
     * The service of returning an Execution object to the queue.
     * Relevant for Resume and Cancel after paused.
     *
     * @param execution   - the Execution to return to the queue.
     * @param queueStatus - the status that the execution will get in the queue
     */
//    public void putExecutionToQueue(Execution execution, ExecStatus queueStatus);

    public Execution getExecutionObj(ExecutionSummaryEntity entity);

    public void setExecutionObj(ExecutionSummaryEntity entity, Execution execution);

    /**
     * Creates a new Executions in the DB.
     *
     * @param executionSummaries : execution summary of a run.
     * @return : the execution id's of executions that were saved.
     */
    public List<String> createExecutionsSummaries(Collection<ExecutionSummaryEntity> executionSummaries);
}