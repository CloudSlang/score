/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

package io.cloudslang.orchestrator.services;

import io.cloudslang.score.facade.execution.ExecutionSummary;
import io.cloudslang.score.facade.execution.PauseReason;
import io.cloudslang.score.facade.entities.Execution;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: stoneo
 * Date: 14/07/2014
 * Time: 11:13
 * !!!!please don't remove the explanation until the implementation is done!
 * this implementation is temporary.
 * the implementation is currently in OO -
 * we plan to have it implemented in the future
 * until it done, no pause and resume is possible in score
 */
public class StubPauseResumeServiceImpl implements PauseResumeService {

    @Override
    public Long pauseExecution(Long executionId, String branchId, PauseReason reason) {
        return null;
    }

    @Override
    public void resumeExecution(Long executionId, String branchId, Map<String, Serializable> map) {

    }

    @Override
    public PauseReason writeExecutionObject(Long executionId, String branchId, Execution execution) {
        return null;
    }

    @Override
    public Set<String> readAllPausedExecutionBranchIds() {
        return null;
    }

    @Override
    public ExecutionSummary readPausedExecution(Long executionId, String branchId) {
        return null;
    }

    @Override
    public List<Long> readPauseIds(Long executionId) {
        return null;
    }
}
