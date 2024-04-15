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

import java.util.List;

/**
 * Date: 8/1/11
 *
 *
 * Responsible for handling the execution
 *
 */
public interface ExecutionService {

    /**
     *
     * Execute the given execution
     *
     * @param execution the {@link io.cloudslang.score.facade.entities.Execution} to execute
     * @return the {@link io.cloudslang.score.facade.entities.Execution} after executing
     * @throws InterruptedException
     */
    Execution execute(Execution execution) throws InterruptedException;

    /**
     * This method MUST be used ONLY for pausing sequential executions.
     * @param execution the {@link io.cloudslang.score.facade.entities.Execution} to pause
     * @throws InterruptedException
     */
    void pauseSequentialExecution(Execution execution) throws InterruptedException;

    void postExecutionWork(Execution execution) throws InterruptedException;

    /**
     *
     * Handles execution of split step
     *
     * @param execution the split {@link io.cloudslang.score.facade.entities.Execution} to execute
     * @return the List of {@link io.cloudslang.score.facade.entities.Execution} that the split returns
     * returns null in case this execution is paused or cancelled and the split was not done
     * @throws InterruptedException
     */
    List<Execution> executeSplitForNonBlockAndParallel(Execution execution) throws InterruptedException;

    List<Execution> executeSplitForMiAndParallelLoop(Execution execution,
                                                     String splitId,
                                                     int nrOfAlreadyCreatedBranches,
                                                     String splitDataKey) throws InterruptedException;

    boolean isSplitStep(Execution execution);
}
