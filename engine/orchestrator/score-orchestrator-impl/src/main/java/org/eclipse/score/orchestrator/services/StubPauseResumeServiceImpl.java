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
package org.eclipse.score.orchestrator.services;

import org.eclipse.score.facade.execution.ExecutionSummary;
import org.eclipse.score.facade.execution.PauseReason;
import org.eclipse.score.facade.entities.Execution;

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
