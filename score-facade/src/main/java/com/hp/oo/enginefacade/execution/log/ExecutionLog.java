package com.hp.oo.enginefacade.execution.log;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
import com.hp.oo.enginefacade.execution.ExecutionSummary;
import com.hp.oo.internal.sdk.execution.RecordBoundInput;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.lang.Object;import java.lang.Override;import java.lang.String;import java.lang.SuppressWarnings;import java.util.HashMap;
import java.util.List;

/**
 * User: zruya
 * Date: 25/12/12
 * Time: 17:30
 */
@SuppressWarnings("UnusedDeclaration")
public class ExecutionLog implements Serializable {
    private ExecutionSummary executionSummary;
    private ExecutionEnums.LogLevel executionLogLevel;
    private HashMap<String, String> flowInputs;
    private List<RecordBoundInput> flowVars;
    private HashMap<String, String> flowOutput;

    public ExecutionSummary getExecutionSummary() {
        return executionSummary;
    }

    public void setExecutionSummary(ExecutionSummary executionSummary) {
        this.executionSummary = executionSummary;
    }

    public ExecutionEnums.LogLevel getExecutionLogLevel() {
        return executionLogLevel;
    }

    public void setExecutionLogLevel(ExecutionEnums.LogLevel executionLogLevel) {
        this.executionLogLevel = executionLogLevel;
    }

    public HashMap<String, String> getFlowInputs() {
        return flowInputs;
    }

    public void setFlowInputs(HashMap<String, String> flowInputs) {
        this.flowInputs = flowInputs;
    }

    public List<RecordBoundInput> getFlowVars() {
        return flowVars;
    }

    public void setFlowVars(List<RecordBoundInput> flowVars) {
        this.flowVars = flowVars;
    }

    public HashMap<String, String> getFlowOutput() {
        return flowOutput;
    }

    public void setFlowOutput(HashMap<String, String> flowOutput) {
        this.flowOutput = flowOutput;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExecutionLog)) return false;

        ExecutionLog that = (ExecutionLog) o;

        return new EqualsBuilder()
                .append(executionSummary, that.executionSummary)
                .append(flowInputs, that.flowInputs)
                .append(executionLogLevel, that.executionLogLevel)
                .append(flowVars, that.flowVars)
                .append(flowOutput, that.flowOutput)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(executionSummary)
                .append(executionLogLevel)
                .append(flowInputs)
                .append(flowVars)
                .append(flowOutput)
                .toHashCode();
    }
}
