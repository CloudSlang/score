package com.hp.score.orchestrator.services;

import com.hp.score.facade.execution.ExecutionStatus;
import com.hp.score.facade.execution.PauseReason;
import com.hp.score.orchestrator.entities.ExecutionState;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

/**
 * User: maromg
 * Date: 29/05/2014
 */
public class ScorePauseResumeImpl implements ScorePauseResume {

    @Autowired
    private ExecutionStateService executionStateService;

    @Autowired
    private PauseResumeService pauseResumeService;

    @Override
    public boolean pauseExecution(Long executionId) {
        ExecutionState executionState = executionStateService.readByExecutionIdAndBranchId(executionId, ExecutionState.EMPTY_BRANCH);
        if (canBePaused(executionState)) {
            pauseResumeService.pauseExecution(executionId, null, PauseReason.USER_PAUSED);
            return true;
        } else {
            return false;
        }
    }

    private boolean canBePaused(ExecutionState executionState) {
        return executionState != null && executionState.getStatus().equals(ExecutionStatus.RUNNING);
    }

    @Override
    public void resumeExecution(Long executionId, Map<String, Serializable> context, Map<String, Serializable> runtimeValues) {
        //todo impl
    }

}
