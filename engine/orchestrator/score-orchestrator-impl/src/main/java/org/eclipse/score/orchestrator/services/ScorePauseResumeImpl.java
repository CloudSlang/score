/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package org.eclipse.score.orchestrator.services;

import org.eclipse.score.facade.execution.ExecutionStatus;
import org.eclipse.score.facade.execution.PauseReason;
import org.eclipse.score.orchestrator.entities.ExecutionState;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.Map;

/**
 * User:
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
        throw new RuntimeException("score resume not implemented yet!");
    }

}
