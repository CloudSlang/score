package com.hp.score.orchestrator.services;

import com.hp.score.engine.queue.entities.ExecStatus;
import com.hp.score.engine.queue.entities.ExecutionMessageConverter;
import com.hp.score.engine.queue.services.QueueDispatcherService;
import com.hp.score.facade.execution.ExecutionStatus;
import com.hp.score.facade.entities.Execution;
import com.hp.oo.internal.sdk.execution.ExecutionConstants;
import com.hp.score.orchestrator.entities.ExecutionState;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static com.hp.score.facade.execution.ExecutionSummary.EMPTY_BRANCH;

/**
 * Created with IntelliJ IDEA.
 * User: hajyhia
 * Date: 3/29/13
 * Time: 10:47 AM
 */
public final class CancelExecutionServiceImpl implements CancelExecutionService {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private ExecutionSerializationUtil executionSerializationUtil;

    @Autowired
    private ExecutionMessageConverter executionMessageConverter;

    @Autowired
    private QueueDispatcherService queueDispatcherService;

    @Autowired
    private ExecutionStateService executionStateService;

    @Override
    @Transactional
    public boolean requestCancelExecution(Long executionId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Cancelling Execution Id: " + executionId);
        }
        if (executionId == null) {
            throw new IllegalArgumentException("Null is not allowed as input of execution id in cancelExecution()");
        }

        ExecutionState executionStateToCancel = executionStateService.readByExecutionIdAndBranchId(executionId, EMPTY_BRANCH);
        // we must have such execution in the table - the record is created on execution triggering
        if (executionStateToCancel == null) {
            String errMsg = "Failed to cancel execution. Execution id: " + executionId + ".";
            logger.error(errMsg);
            return false;
        }

        ExecutionStatus status = executionStateToCancel.getStatus();

        if (status.equals(ExecutionStatus.CANCELED) || status.equals(ExecutionStatus.PENDING_CANCEL)) {
            return true;
        }

        // it's possible to cancel only running or paused executions.
        // If it's running - set to pending-cancel, and the ExecutionServiceImpl will handle it and extract it from the queue.
        // If it's paused - sometimes needs to handle its branches (if such exists).
        if (status.equals(ExecutionStatus.RUNNING)) {
            executionStateToCancel.setStatus(ExecutionStatus.PENDING_CANCEL);
        } else if (status.equals(ExecutionStatus.PAUSED)) {

            cancelPausedRun(executionStateToCancel);

        } else {
            String errMsg = "Failed to cancel execution. Execution id: " + executionId + ". Execution is in status: " + executionStateToCancel.getStatus().name();
            logger.error(errMsg);
            return false;
        }

        return true;
    }

    // Cancel paused run according to its branches state
    //      If the run has branches, (it can be branch-paused / user-paused / no-workers-in-group) - then it's a 'virtual' pause, and we should cancel the paused branches. Then, the run itself will be canceled as well.
    //      If it doesn't - just cancel it straight away - extract the Run Object, set its context accordingly and put into the queue.
    private void cancelPausedRun(ExecutionState executionStateToCancel) {
        final List<ExecutionState> branches = executionStateService.readByExecutionId(executionStateToCancel.getExecutionId());

        // If the parent is paused because one of the branches is paused, OR, it was paused by the user / no-workers-in-group, but has branches that were not finished (and thus, were paused) -
        // The parent itself will return to the queue after all the branches are ended (due to this cancellation), and then it'll be canceled as well.
        if (branches.size() > 1) { // more than 1 means that it has paused branches (branches is at least 1 - the parent)
            for (ExecutionState branch : branches) {
                if (!EMPTY_BRANCH.equals(branch.getBranchId())) { // exclude the base execution
                    returnCanceledRunToQueue(branch);
                }
            }
            executionStateToCancel.setStatus(ExecutionStatus.PENDING_CANCEL); // when the parent will return to queue - should have the correct status
        } else {
            returnCanceledRunToQueue(executionStateToCancel);
        }
    }

    private void returnCanceledRunToQueue(ExecutionState executionStateToCancel) {
        // set the context and return the run to the queue. It will be handled on "finishFlow" (QueueEventListener).
        Execution executionObj = executionSerializationUtil.objFromBytes(executionStateToCancel.getExecutionObject());
        if (executionObj == null) {
            logger.error("Run Object is null. Execution Id = " + executionStateToCancel.getExecutionId() + "; Branch Id = " + executionStateToCancel.getBranchId());
            return;
        }
        executionObj.getSystemContext().put(ExecutionConstants.FLOW_TERMINATION_TYPE, ExecutionStatus.CANCELED);
        executionObj.setPosition(null);

        // just in case - we shouldn't need it, because the Execution is back to the queue as "Terminated"
        executionStateToCancel.setStatus(ExecutionStatus.PENDING_CANCEL);
        // clean the DB field
        executionStateToCancel.setExecutionObject(null);

        // return execution to queue, as "Terminated"
        queueDispatcherService.dispatch(
                String.valueOf(executionObj.getExecutionId()),
                executionObj.getGroupName(),
                ExecStatus.TERMINATED,
                executionMessageConverter.createPayload(executionObj)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCanceledExecution(Long executionId) {
        return executionStateService.readCancelledExecution(executionId) != null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> readCanceledExecutionsIds() {
        List<Long> result = executionStateService.readExecutionIdByStatuses(getCancelStatuses());
        if (result == null) {
            result = Arrays.asList();
        }
        return result;
    }

    private List<ExecutionStatus> getCancelStatuses() {
        return Arrays.asList(ExecutionStatus.CANCELED, ExecutionStatus.PENDING_CANCEL);
    }
}
