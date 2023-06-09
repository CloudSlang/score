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

package io.cloudslang.orchestrator.entities;

import java.io.Serializable;
import java.util.Set;

import static java.util.Collections.emptySet;


public class MergedConfigurationDataContainer implements Serializable {

    private static final long serialVersionUID = 6409757702040555672L;

    private Set<Long> cancelledExecutions;
    private Set<String> pausedExecutions;
    private Set<String> workerGroups;

    public MergedConfigurationDataContainer() {
        this.cancelledExecutions = emptySet();
        this.pausedExecutions = emptySet();
        this.workerGroups = emptySet();
    }

    public MergedConfigurationDataContainer(Set<Long> cancelledExecutions, Set<String> pausedExecutions,
            Set<String> workerGroups) {
        this.cancelledExecutions = cancelledExecutions;
        this.pausedExecutions = pausedExecutions;
        this.workerGroups = workerGroups;
    }

    public Set<Long> getCancelledExecutions() {
        return cancelledExecutions;
    }

    public void setCancelledExecutions(Set<Long> cancelledExecutions) {
        this.cancelledExecutions = cancelledExecutions;
    }

    public Set<String> getPausedExecutions() {
        return pausedExecutions;
    }

    public void setPausedExecutions(Set<String> pausedExecutions) {
        this.pausedExecutions = pausedExecutions;
    }

    public Set<String> getWorkerGroups() {
        return workerGroups;
    }

    public void setWorkerGroups(Set<String> workerGroups) {
        this.workerGroups = workerGroups;
    }
}
