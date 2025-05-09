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

package io.cloudslang.job;

/**
 * User: wahnonm
 * Date: 13/08/14
 * Time: 10:35
 */
public interface ScoreEngineJobs {

    /**
     * job that join all the suspended execution of brunches that finished
     */
    void joinFinishedSplitsJob();

    /**
     * job that update version number - we use it instead of time
     */
    void recoveryVersionJob();

    /**
     * job that recover workers that didn't send keep alive
     */
    void executionRecoveryJob();

    void cleanSuspendedExecutionsJob();

    void miMergeBranchesContexts();

    void monitorLargeMessagesJob();

    void cleanFinishedExecutionState() ;

    /**
     * Removes suspended executions that have been finished for more than 24 hours and were not automatically cleared.
     */
    void cleanSuspendedExecutions();
}
