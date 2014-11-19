/*
 * Licensed to Hewlett-Packard Development Company, L.P. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
*/
package org.eclipse.score.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * User: stoneo
 * Date: 05/08/2014
 * Time: 14:04
 */
public class TriggeringProperties {

    private ExecutionPlan executionPlan;
    private Map<String, ExecutionPlan> dependencies = new HashMap<>();
    private Map<String, ? extends Serializable> context = new HashMap<>();
    private Map<String, ? extends Serializable> runtimeValues = new HashMap<>();
    private Long startStep;

    private TriggeringProperties(ExecutionPlan executionPlan){
        this.executionPlan = executionPlan;
        this.startStep = executionPlan.getBeginStep();
    }

    public static TriggeringProperties create(ExecutionPlan executionPlan){
        return new TriggeringProperties(executionPlan);
    }

    public TriggeringProperties setDependencies(Map<String, ExecutionPlan> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public TriggeringProperties setContext(Map<String, ? extends Serializable> context) {
        this.context = context;
        return this;
    }

    public TriggeringProperties setRuntimeValues(Map<String, ? extends Serializable> runtimeValues) {
        this.runtimeValues = runtimeValues;
        return this;
    }

    public TriggeringProperties setStartStep(Long startStep) {
        this.startStep = startStep;
        return this;
    }

    public ExecutionPlan getExecutionPlan() {
        return executionPlan;
    }

    public Map<String, ExecutionPlan> getDependencies() {
        return dependencies;
    }

    public Map<String, ? extends Serializable> getContext() {
        return context;
    }

    public Map<String, ? extends Serializable> getRuntimeValues() {
        return runtimeValues;
    }

    public Long getStartStep() {
        return startStep;
    }
}
