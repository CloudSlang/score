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

import io.cloudslang.score.facade.entities.Execution;
import io.cloudslang.orchestrator.entities.SplitMessage;

import java.util.List;

/**
 * User: zruya
 * Date: 22/08/13
 * Time: 14:31
 */
public interface SplitJoinService {

    /**
     * Triggers the child executions and suspends the parent execution
     * while terminating the step that created the split
     *
     * @param messages a list of ForkCommands
     */
    void split(List<SplitMessage> messages);

    /**
     * Persists the branch that ended to the DB
     *
     * @param executions finished branches
     */
    void endBranch(List<Execution> executions);

    /**
     * Collects all finished forks from the DB,
     * merges the children into the parent executions,
     * and triggers the parent Executions back to the queue.
     *
     * This will be launched using a singleton quartz job
     *
     * @param bulkSize the amount of finished splits to join
     * @return actual amount of joined splits
     */
    int joinFinishedSplits(int bulkSize);

    /**
     * Collects all finished forks from the DB,
     * merges the children into the parent executions,
     * and triggers the parent Executions back to the queue.
     *
     */
    void joinFinishedSplits();

    int joinFinishedMiBranches(int bulkSize);
}
