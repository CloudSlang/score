package com.hp.oo.orchestrator.services;

import com.hp.oo.engine.queue.entities.ExecStatus;
import com.hp.oo.engine.queue.entities.ExecutionMessageConverter;
import com.hp.oo.engine.queue.services.QueueDispatcherService;
import com.hp.oo.enginefacade.execution.ExecutionEnums.ExecutionStatus;
import com.hp.oo.internal.sdk.execution.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.oo.orchestrator.entities.ExecutionSummaryEntity;
import com.hp.oo.orchestrator.repositories.ExecutionSummaryRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.hp.oo.enginefacade.execution.ExecutionSummary.EMPTY_BRANCH;

/**
 * Created with IntelliJ IDEA.
 * User: hajyhia
 * Date: 3/29/13
 * Time: 10:47 AM
 */
@Service("cancelExecutionService")
public final class CancelExecutionServiceImpl implements CancelExecutionService {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private ExecutionSummaryRepository repository;

    @Autowired
    private ExecutionSerializationUtil executionSerializationUtil;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Autowired
    private QueueDispatcherService queueDispatcherService;

    @Override
    @Transactional
    public boolean requestCancelExecution(String executionId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Cancelling Execution Id: " + executionId);
        }
        if (executionId == null) {
            throw new IllegalArgumentException("Null is not allowed as input of execution id in cancelExecution()");
        }

        ExecutionSummaryEntity executionToCancel = repository.findByExecutionIdAndBranchId(executionId, EMPTY_BRANCH);
        // we must have such execution in the table - the record is created on execution triggering
        if (executionToCancel == null) {
            String errMsg = "Failed to cancel execution. Execution id: " + executionId + ".";
            logger.error(errMsg);
            return false;
        }

        ExecutionStatus status = executionToCancel.getStatus();

        if (status.equals(ExecutionStatus.CANCELED) || status.equals(ExecutionStatus.PENDING_CANCEL)) {
            return true;
        }

        // it's possible to cancel only running or paused executions.
        // If it's running - set to pending-cancel, and the ExecutionServiceImpl will handle it and extract it from the queue.
        // If it's paused - sometimes needs to handle its branches (if such exists).
        if (status.equals(ExecutionStatus.RUNNING)) {
            executionToCancel.setStatus(ExecutionStatus.PENDING_CANCEL);
        } else if (status.equals(ExecutionStatus.PAUSED)) {

            cancelPausedExecution(executionToCancel);

        } else {
            String errMsg = "Failed to cancel execution. Execution id: " + executionId + ". Execution is in status: " + executionToCancel.getStatus().name();
            logger.error(errMsg);
            return false;
        }

        return true;
    }

    // Cancel paused execution according to its branches state
    //      If the execution has branches, (it can be branch-paused / user-paused / no-workers-in-group) - then it's a 'virtual' pause, and we should cancel the paused branches. Then, the run itself will be canceled as well.
    //      If it doesn't - just cancel it straight away - extract the Execution Object, set its context accordingly and put into the queue.
    private void cancelPausedExecution(ExecutionSummaryEntity executionToCancel) {
        final List<ExecutionSummaryEntity> branches = repository.findByExecutionId(executionToCancel.getExecutionId());

        // If the parent is paused because one of the branches is paused, OR, it was paused by the user / no-workers-in-group, but has branches that were not finished (and thus, were paused) -
        // The parent itself will return to the queue after all the branches are ended (due to this cancellation), and then it'll be canceled as well.
        if (branches.size() > 1) { // more than 1 means that it has paused branches (branches is at least 1 - the parent)
            for (ExecutionSummaryEntity branch : branches) {
                if (!EMPTY_BRANCH.equals(branch.getBranchId())) { // exclude the base execution
                    returnCanceledExecutionToQueue(branch);
                }
            }
            executionToCancel.setStatus(ExecutionStatus.PENDING_CANCEL); // when the parent will return to queue - should have the correct status
        } else {
            returnCanceledExecutionToQueue(executionToCancel);
        }
    }

    private void returnCanceledExecutionToQueue(ExecutionSummaryEntity executionToCancel) {
        // set the context and return the execution to the queue. It will be handled on "finishFlow" (QueueEventListener).
        Execution executionObj = executionSerializationUtil.objFromBytes(executionToCancel.getExecutionObj());
        if (executionObj == null) {
            logger.error("Execution Object is null. Execution Id = " + executionToCancel.getExecutionId() + "; Branch Id = " + executionToCancel.getBranchId());
            return;
        }
        executionObj.getSystemContext().put(ExecutionConstants.FLOW_TERMINATION_TYPE, ExecutionStatus.CANCELED);
        executionObj.setPosition(null);

        // just in case - we shouldn't need it, because the Execution is back to the queue as "Terminated"
        executionToCancel.setStatus(ExecutionStatus.PENDING_CANCEL);
        // clean the DB field
        executionToCancel.setExecutionObj(null);

        // return execution to queue, as "Terminated"
        queueDispatcherService.dispatch(
                executionObj.getExecutionId(),
                executionObj.getGroupName(),
                ExecStatus.TERMINATED,
                executionMessageConverter.createPayload(executionObj)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCanceledExecution(String executionId) {
        return repository.findByExecutionIdAndBranchIdAndStatusIn(executionId, EMPTY_BRANCH, getCancelStatuses()) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> readCanceledExecutionsIds() {
        List<ExecutionSummaryEntity> executions = repository.findByStatusIn(getCancelStatuses());
        if (executions == null || executions.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> ids = new ArrayList<>(executions.size());
        for (ExecutionSummaryEntity exec : executions) {
            ids.add(exec.getExecutionId());
        }
        return ids;
    }

    private List<ExecutionStatus> getCancelStatuses() {
        return Arrays.asList(ExecutionStatus.CANCELED, ExecutionStatus.PENDING_CANCEL);
    }
}
