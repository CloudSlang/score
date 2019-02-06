/*
 * Copyright © 2014-2017 EntIT Software LLC, a Micro Focus company (L.P.)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudslang.worker.execution.services;

import io.cloudslang.orchestrator.services.ExecutionStateService;
import io.cloudslang.orchestrator.services.PauseResumeService;
import io.cloudslang.score.facade.entities.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public final class ExternalExecutionServiceImpl implements ExternalExecutionService {

    @Autowired
    private PauseResumeService pauseService;

    @Autowired
    private ExecutionStateService stateService;

    @Override
    public void resumeExternalExecution(Execution execution) {
        pauseService.resumeExecution(execution.getExecutionId(),
                execution.getSystemContext().getBranchId(), new HashMap<>());
    }

    @Override
    public Execution readExecutionObject(Long executionId, String branchId) {
        return stateService.readExecutionObject(executionId, branchId);
    }

    @Override
    public void updateExecutionObject(Long executionId, String branchId, Execution execution) {
        stateService.updateExecutionObject(executionId, branchId, execution);
    }

}
