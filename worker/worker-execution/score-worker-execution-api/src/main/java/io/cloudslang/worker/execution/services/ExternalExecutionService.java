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

import io.cloudslang.score.facade.entities.Execution;

import java.util.Date;

public interface ExternalExecutionService {
    void pauseExternalExecution(Execution execution) throws InterruptedException;

    void resumeExternalExecution(Execution execution) throws InterruptedException;

    Execution readExecutionObject(Long executionId, String branchId);

    void updateExecutionObject(Long executionId, String branchId, Execution execution, Date updateDate);

    void postExecutionWork(Execution execution) throws InterruptedException;
}
