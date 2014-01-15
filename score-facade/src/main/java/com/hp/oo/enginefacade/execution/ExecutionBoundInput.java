package com.hp.oo.enginefacade.execution;

import java.io.Serializable;
import java.util.Set;

public class ExecutionBoundInput implements Serializable{
	private static final long serialVersionUID = 8220624776561835231L;

	private String inputName;

    private String domainTermName;

    private String value;

    private Set<String> executionIds;

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public String getDomainTermName() {
        return domainTermName;
    }

    public void setDomainTermName(String domainTermName) {
        this.domainTermName = domainTermName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<String> getExecutionIds() {
        return executionIds;
    }

    public void setExecutionIds(Set<String> executionIds) {
        this.executionIds = executionIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExecutionBoundInput that = (ExecutionBoundInput) o;

        if (domainTermName != null ? !domainTermName.equals(that.domainTermName) : that.domainTermName != null)
            return false;
        if (executionIds != null ? !executionIds.equals(that.executionIds) : that.executionIds != null) return false;
        if (inputName != null ? !inputName.equals(that.inputName) : that.inputName != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = inputName != null ? inputName.hashCode() : 0;
        result = 31 * result + (domainTermName != null ? domainTermName.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (executionIds != null ? executionIds.hashCode() : 0);
        return result;
    }
}

