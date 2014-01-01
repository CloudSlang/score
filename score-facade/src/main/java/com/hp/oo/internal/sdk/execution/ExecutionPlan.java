package com.hp.oo.internal.sdk.execution;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Date: 8/1/11
 *
 * @author Dima Rassin
 */
public class ExecutionPlan implements Serializable {
    private static final long serialVersionUID = -7685110912034208064L;

    protected String flowUuid;

    protected Long beginStep;

    protected String name;
    //the name of the flow language this execution plan  represents, such as afl
    protected String language;

    protected Map<Long, ExecutionStep> steps = new HashMap<Long, ExecutionStep>();

    //Holds the list of it's direct subflows UUIDs
    protected Set<String> subflowsUUIDs = new HashSet<>();

    public Set<String> getSubflowsUUIDs() {
        return subflowsUUIDs;
    }

    public void setSubflowsUUIDs(Set<String> subflowsUUIDs) {
        this.subflowsUUIDs = subflowsUUIDs;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getFlowUuid() {
        return flowUuid;
    }

    public ExecutionPlan setFlowUuid(String flowUuid) {
        this.flowUuid = flowUuid;
        return this;
    }

    public Long getBeginStep() {
        return beginStep;
    }

    public ExecutionPlan setBeginStep(Long beginStep) {
        this.beginStep = beginStep;
        return this;
    }

    public Map<Long, ExecutionStep> getSteps() {
        return steps;
    }

    public ExecutionStep getStep(Long stepId) {
        return steps.get(stepId);
    }

    public ExecutionPlan addStep(ExecutionStep step) {
        this.steps.put(step.getExecStepId(), step);
        return this;
    }

    public ExecutionPlan addSteps(List<ExecutionStep> steps) {
        for (ExecutionStep curStep : steps) {
            this.steps.put(curStep.getExecStepId(), curStep);
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ExecutionPlan: \n" +
                "FlowUuid= '" + flowUuid + '\'' +
                "\n BeginStep= " + beginStep +
                "\n Name= '" + name + '\'' +
                "\n Steps: \n" + printSteps();
    }

    private String printSteps() {
        StringBuilder strBld = new StringBuilder();
        for (Map.Entry entry : steps.entrySet()) {
            strBld.append("\t" + entry.getKey() + " -> " + entry.getValue().toString() + "\n");
            strBld.append("______________________________________________________________________________________________________________________________________________________________________________________________\n");
        }
        return strBld.toString();
    }

    @Override
    public boolean equals(Object o) {
        EqualsBuilder equalsBuilder = new EqualsBuilder();

        if(!(o instanceof ExecutionPlan)){
            return false;
        }

        ExecutionPlan other = (ExecutionPlan)o;

        equalsBuilder.append(this.getFlowUuid(), other.getFlowUuid());
        equalsBuilder.append(this.getBeginStep(), other.getBeginStep());
        equalsBuilder.append(this.getName(), other.getName());
        equalsBuilder.append(this.getLanguage(), other.getLanguage());
        equalsBuilder.append(this.getSubflowsUUIDs(), other.getSubflowsUUIDs());
        equalsBuilder.append(this.getSteps(), other.getSteps());

        return equalsBuilder.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();

        hashCodeBuilder.append(this.getFlowUuid());
        hashCodeBuilder.append(this.getBeginStep());
        hashCodeBuilder.append(this.getName());
        hashCodeBuilder.append(this.getLanguage());
        hashCodeBuilder.append(this.getSubflowsUUIDs());
        hashCodeBuilder.append(this.getSteps());
        
        return new HashCodeBuilder().toHashCode();
    }
}
