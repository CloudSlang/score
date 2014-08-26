package com.hp.score.orchestrator.entities;

import com.hp.oo.enginefacade.execution.ExecutionStatus;
import com.hp.oo.enginefacade.execution.ExecutionSummary;
import com.hp.score.engine.data.AbstractIdentifiable;
import org.apache.commons.lang.builder.EqualsBuilder;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Arrays;

/**
 * User: maromg
 * Date: 12/05/2014
 */
@Entity
@Table(name = "OO_EXECUTION_STATE",
		uniqueConstraints = {@UniqueConstraint(name = "OO_EXECUTION_STATE_UC", columnNames = {"EXECUTION_ID", "BRANCH_ID"})})
public class ExecutionState extends AbstractIdentifiable {

    public static final String EMPTY_BRANCH = "EMPTY";

    @Column(name = "EXECUTION_ID", nullable = false)
    private Long executionId;

    @Column(name = "BRANCH_ID", nullable = false)
    private String branchId = ExecutionSummary.EMPTY_BRANCH;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ExecutionStatus status;

    @Column(name = "EXECUTION_OBJECT")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] executionObject;

    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }

    public byte[] getExecutionObject() {
        return executionObject;
    }

    public void setExecutionObject(byte[] executionObj) {
        this.executionObject = executionObj;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ExecutionState)) return false;

        ExecutionState that = (ExecutionState) obj;
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(branchId, that.branchId);
        equalsBuilder.append(executionId, that.executionId);
        equalsBuilder.append(executionObject, that.executionObject);
        equalsBuilder.append(status, that.status);
        return equalsBuilder.isEquals();
    }

    @Override
    public int hashCode() {
        int result = executionId != null ? executionId.hashCode() : 0;
        result = 31 * result + (branchId != null ? branchId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (executionObject != null ? Arrays.hashCode(executionObject) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExecutionState {" +
                "executionId='" + executionId + '\'' +
                ", branchId='" + branchId + '\'' +
                '}';
    }
}
