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
package io.cloudslang.worker.management;

/**
 * @author kravtsov
 * @author Avi Moradi
 * @since 03/06/2012
 * Used by Score for pause/cancel runs & stay in the worker
 */
public interface WorkerConfigurationService {

    /**
     * checks if the given execution is pending cancel
     *
     * @param executionId the execution id to check
     * @return true if the execution is pending cancel
     */
    boolean isExecutionCancelled(Long executionId);

    /**
     * checks if the given execution is pending pause
     *
     * @param executionId the execution id to check
     * @return true if the execution is pending pause
     */
    boolean isExecutionPaused(Long executionId, String branchId);

    /**
     *
     * checks if the current worker is part of the fiven group
     *
     * @param group the group to check
     * @return true if the worker is part of the group
     */
    boolean isMemberOf(String group);

    /**
     * Sets the current worker enabled state
     * relevant for tasks not to work until the worker is enabled
     *
     * @param enabled the edibility state to set
     */
    void setEnabled(boolean enabled);

}
