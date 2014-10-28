/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
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
        throw new RuntimeException("score resume not implemented yet!");
    }

}
