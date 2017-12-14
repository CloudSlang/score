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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MergedConfigurationDataContainer implements Serializable{
    private volatile List<Long> cancelledExecutions;
    private volatile Set<String> pausedExecutions;
    private volatile List<String> workerGroups;

    public MergedConfigurationDataContainer() {
    }



    public List<Long> getCancelledExecutions() {
        return cancelledExecutions;
    }

    public void setCancelledExecutions(List<Long> cancelledExecutions) {
        this.cancelledExecutions = cancelledExecutions;
    }

    public Set<String> getPausedExecutions() {
        return pausedExecutions;
    }

    public void setPausedExecutions(Set<String> pausedExecutions) {
        this.pausedExecutions = pausedExecutions;
    }

    public List<String> getWorkerGroups() {
        return workerGroups;
    }

    public void setWorkerGroups(List<String> workerGroups) {
        this.workerGroups = workerGroups;
    }

}
