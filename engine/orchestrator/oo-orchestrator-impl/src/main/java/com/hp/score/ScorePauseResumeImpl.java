package com.hp.score;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.PauseReason;
import com.hp.oo.orchestrator.services.PauseResumeService;
import com.hp.score.entities.RunState;
import com.hp.score.services.RunStateService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

/**
 * User: maromg
 * Date: 29/05/2014
 */
public class ScorePauseResumeImpl implements ScorePauseResume {

    @Autowired
    private RunStateService runStateService;

    @Autowired
    private PauseResumeService pauseResumeService;

    @Override
    public boolean pauseExecution(Long executionId) {
        RunState runState = runStateService.readByRunIdAndBranchId(executionId.toString(), RunState.EMPTY_BRANCH);
        if (canBePaused(runState)) {
            pauseResumeService.pauseExecution(executionId.toString(), null, PauseReason.USER_PAUSED);
            return true;
        } else {
            return false;
        }
    }

    private boolean canBePaused(RunState runState) {
        return runState != null && runState.getStatus().equals(ExecutionEnums.ExecutionStatus.RUNNING);
    }

    @Override
    public void resumeExecution(Long executionId, Map<String, Serializable> context, Map<String, Serializable> systemContext) {
        //todo impl
    }

}
