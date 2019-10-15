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
package io.cloudslang.orchestrator.services;

import io.cloudslang.orchestrator.entities.ExecutionObjEntity;
import io.cloudslang.orchestrator.entities.SuspendedExecution;
import io.cloudslang.orchestrator.repositories.SuspendedExecutionsRepository;
import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.score.lang.SystemContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Map;

@Service
public class SuspendedExecutionServiceImpl implements SuspendedExecutionService {

    private final SuspendedExecutionsRepository suspendedExecutionsRepository;

    @Autowired
    public SuspendedExecutionServiceImpl(SuspendedExecutionsRepository suspendedExecutionsRepository) {
        this.suspendedExecutionsRepository = suspendedExecutionsRepository;
    }

    @Override
    @Transactional
    public void updateSuspendedExecutionMiThrottlingContext(Execution execution) {
        SuspendedExecution suspendedExecution = suspendedExecutionsRepository.findByExecutionId(execution.getExecutionId().toString());
        if (suspendedExecution != null) {
            Execution oldExecution = suspendedExecution.getExecutionObj();
            execution.setPosition(oldExecution.getPosition());
            suspendedExecutionsRepository.updateSuspendedExecutionContexts(new ExecutionObjEntity(execution));
        }
    }

    @Override
    @Transactional
    public void unlockSuspendedExecution(String executionId) {
        SuspendedExecution suspendedExecution = suspendedExecutionsRepository.findByExecutionId(executionId);
        if (suspendedExecution != null) {
            suspendedExecution.setLocked(false);
        }
    }

}
