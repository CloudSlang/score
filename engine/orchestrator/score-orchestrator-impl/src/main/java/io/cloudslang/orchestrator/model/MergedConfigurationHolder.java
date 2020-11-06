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
package io.cloudslang.orchestrator.model;


import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;

public class MergedConfigurationHolder {

    private final Set<Long> cancelledExecutions;
    private final Set<String> pausedExecutionBranchIdPairs;
    private final Map<String, Set<String>> workerGroupsMap;

    public MergedConfigurationHolder() {
        this.cancelledExecutions = emptySet();
        this.pausedExecutionBranchIdPairs = emptySet();
        this.workerGroupsMap = emptyMap();
    }

    public MergedConfigurationHolder(Set<Long> cancelledExecutions, Set<String> pausedExecutions,
            Map<String, Set<String>> workerGroupsMap) {
        this.cancelledExecutions = cancelledExecutions;
        this.pausedExecutionBranchIdPairs = pausedExecutions;
        this.workerGroupsMap = workerGroupsMap;
    }

    public Set<Long> getCancelledExecutions() {
        return cancelledExecutions;
    }

    public Set<String> getPausedExecutionBranchIdPairs() {
        return pausedExecutionBranchIdPairs;
    }

    public Set<String> getWorkerGroupsForWorker(String workerId) {
        Set<String> groups = workerGroupsMap.get(workerId);
        return (groups != null) ? groups : emptySet();
    }

}
