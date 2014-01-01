package com.hp.oo.orchestrator.entities;

import com.hp.score.engine.data.AbstractIdentifiable;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "OO_EXECUTION_BOUND_INPUTS")
public class ExecutionBoundInputEntity extends AbstractIdentifiable {

    @Column(name = "INPUT_NAME", nullable = false)
    private String inputName;

    @Column(name = "DOMAIN_TERM_NAME", nullable = true)
    private String domainTermName;

    @Column(name = "VALUE", nullable = true)
    private String value;

    @Column(name = "EXECUTION_ID", nullable = true)
    @ElementCollection
    @CollectionTable(name = "OO_EXECUTION_BOUND_INPUTS_IDS", joinColumns = {@JoinColumn(name = "EXECUTION_BOUND_INPUT_ID", nullable = false)})
    private Set<String> executionIds = new HashSet<>();

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

        ExecutionBoundInputEntity that = (ExecutionBoundInputEntity) o;

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
