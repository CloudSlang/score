package com.hp.score.entities;

import com.hp.oo.enginefacade.execution.ExecutionEnums;
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
@Table(name = "SCORE_RUN_STATE",
        uniqueConstraints = {@UniqueConstraint(name = "SCORE_RUN_STATE_UC", columnNames = {"RUN_ID", "BRANCH_ID"})})
public class RunState extends AbstractIdentifiable {

    @Column(name = "RUN_ID", nullable = false)
    private String runId;

    @Column(name = "BRANCH_ID", nullable = false)
    private String branchId = ExecutionSummary.EMPTY_BRANCH;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ExecutionEnums.ExecutionStatus status;

    @Column(name = "RUN_OBJECT")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] runObject;

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public ExecutionEnums.ExecutionStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutionEnums.ExecutionStatus status) {
        this.status = status;
    }

    public byte[] getRunObject() {
        return runObject;
    }

    public void setRunObject(byte[] executionObj) {
        this.runObject = executionObj;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof RunState)) return false;

        RunState that = (RunState) obj;
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        equalsBuilder.append(branchId, that.branchId);
        equalsBuilder.append(runId, that.runId);
        equalsBuilder.append(runObject, that.runObject);
        equalsBuilder.append(status, that.status);
        return equalsBuilder.isEquals();
    }

    @Override
    public int hashCode() {
        int result = runId != null ? runId.hashCode() : 0;
        result = 31 * result + (branchId != null ? branchId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (runObject != null ? Arrays.hashCode(runObject) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RunState{" +
                "runId='" + runId + '\'' +
                ", branchId='" + branchId + '\'' +
                '}';
    }
}
