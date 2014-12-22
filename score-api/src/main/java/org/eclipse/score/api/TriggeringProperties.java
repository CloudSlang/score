/*******************************************************************************
* (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Apache License v2.0 which accompany this distribution.
*
* The Apache License is available at
* http://www.apache.org/licenses/LICENSE-2.0
*
*******************************************************************************/

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
