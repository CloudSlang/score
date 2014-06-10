package com.hp.score;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.orchestrator.services.PauseResumeService;
import com.hp.score.entities.ExecutionState;
import com.hp.score.services.ExecutionStateService;
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
        ExecutionState executionState = executionStateService.readByExecutionIdAndBranchId(executionId.toString(), ExecutionState.EMPTY_BRANCH);
        if (canBePaused(executionState)) {
            pauseResumeService.pauseExecution(executionId.toString(), null, PauseReason.USER_PAUSED);
            return true;
        } else {
            return false;
        }
    }

    private boolean canBePaused(ExecutionState executionState) {
        return executionState != null && executionState.getStatus().equals(ExecutionEnums.ExecutionStatus.RUNNING);
    }

    @Override
    public void resumeExecution(Long executionId, Map<String, Serializable> context, Map<String, Serializable> runtimeValues) {
        //todo impl
    }

}
