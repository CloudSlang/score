package com.hp.oo.internal.sdk.execution;


import java.io.Serializable;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Meir Wahnon
 * Date: 11/22/11
 * Time: 2:29 PM
 */
public class ExecutionPlanContainer implements Serializable {

    private static final long serialVersionUID = -2761637762150765269L;

    private ExecutionPlan executionPlan;

    private List<FlowInput> flowInputs;

    public ExecutionPlanContainer() {}

    public ExecutionPlan getExecutionPlan() {
        return executionPlan;
    }

    public void setExecutionPlan(ExecutionPlan executionPlan) {
        this.executionPlan = executionPlan;
    }

    public List<FlowInput> getFlowInputs() {
        return flowInputs;
    }

    public void setFlowInputs(List<FlowInput> flowInputs) {
        this.flowInputs = flowInputs;
    }
}
